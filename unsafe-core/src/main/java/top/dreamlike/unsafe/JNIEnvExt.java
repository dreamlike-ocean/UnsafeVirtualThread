package top.dreamlike.unsafe;

import top.dreamlike.unsafe.helper.JValue;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import static top.dreamlike.unsafe.JNIEnv.CallMethodByNameFp;
import static top.dreamlike.unsafe.JNIEnv.CallStaticMethodByNameFp;

class JNIEnvExt {

    static final MethodHandle CallMethodByNameSingleArgsMH = Linker.nativeLinker()
            .downcallHandle(FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    /*JNIEnv *env */ValueLayout.ADDRESS,
                    /*jboolean *hasException*/ValueLayout.ADDRESS,
                    /* jobject*/ ValueLayout.ADDRESS,
                    /*const char *name*/ ValueLayout.ADDRESS,
                    /* const char *signature*/ ValueLayout.ADDRESS,
                    /* jvalue *args*/ ValueLayout.ADDRESS
            )).bindTo(CallMethodByNameFp);
    // Class<?> name = Class.forName("top.dreamlike.unsafe.JNIEnv", true, loader);
    static final MethodHandle ClassLoaderForNameMH = Linker.nativeLinker()
            .downcallHandle(FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    /*JNIEnv *env */ValueLayout.ADDRESS,
                    /*jboolean *hasException*/ValueLayout.ADDRESS,
                    /* const char *classname*/ ValueLayout.ADDRESS,
                    /*const char *name*/ ValueLayout.ADDRESS,
                    /* const char *signature*/ ValueLayout.ADDRESS,
                    /*jstring className*/ ValueLayout.ADDRESS,
                    /*jboolean initialize*/ ValueLayout.JAVA_BOOLEAN,
                    /*jobject loader*/ ValueLayout.ADDRESS
            )).bindTo(CallStaticMethodByNameFp);

}
