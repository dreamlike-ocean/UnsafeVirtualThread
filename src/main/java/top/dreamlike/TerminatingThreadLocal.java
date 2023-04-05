package top.dreamlike;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TerminatingThreadLocal<T> extends ThreadLocal<T> {

    private final static MethodHandle internalConstructorMH = init();


    private final ThreadLocal<T> internal;

    public TerminatingThreadLocal() {
        try {
            internal = (ThreadLocal<T>) internalConstructorMH.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }




    @Override
    public T get() {
        return internal.get();
    }

    @Override
    public void set(T value) {
        internal.set(value);
    }

    @Override
    public void remove() {
        internal.remove();
    }

    protected void threadTerminated(T value) {
        System.out.println(value+":end");
    }


    private static MethodHandle init() {
       try {
           String className = "jdk.internal.misc.TerminatingThreadLocal";
           Class<?> threadLocalClass = Class.forName(className);
           Module module = threadLocalClass.getModule();
           //需要一个内部的后门打开这个模块。。。太hack了 要不没法load我搓出来的类
           Field accessField = Class.forName("jdk.internal.access.SharedSecrets")
                   .getDeclaredField("javaLangAccess");
           Object o = VirtualThreadUnsafe
                   .IMPL_LOOKUP
                   .unreflectVarHandle(accessField)
                   .get();
           //void addExports(Module m1, String pkg);
           Method addExports = Class.forName("jdk.internal.access.JavaLangAccess")
                   .getDeclaredMethod("addExports", Module.class, String.class);
           //无条件打开java.base 的 jdk.internal.misc
           VirtualThreadUnsafe.IMPL_LOOKUP
                   .unreflect(addExports)
                   .invoke(o, module, "jdk.internal.misc");


           ByteBuddy buddy = new ByteBuddy();

           Class<?> customerTerminatingThreadLocalClass = buddy.subclass(threadLocalClass)
                   .method(ElementMatchers.named("threadTerminated"))
                   //todo 转发到我们搞得类上面。。。
                   .intercept(MethodDelegation.to(Main.Proxy.class))
                   .make()
                   .load(TerminatingThreadLocal.class.getClassLoader())
                   .getLoaded();

           return VirtualThreadUnsafe
                   .IMPL_LOOKUP
                   .in(customerTerminatingThreadLocalClass)
                   .findConstructor(customerTerminatingThreadLocalClass, MethodType.methodType(void.class));
       }catch (Throwable throwable) {
           throw new RuntimeException(throwable);
       }
    }

}
