package datadog.trace.civisibility.coverage.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CoverageUtils {

  public static void insertCoverageProbe(String className, MethodVisitor mv) {
    String typeDescriptor = 'L' + className + ';';
    Type type = Type.getType(typeDescriptor);
    mv.visitLdcInsn(type);

    mv.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "datadog/trace/api/civisibility/coverage/CoverageBridge",
        "recordCoverage",
        "(Ljava/lang/Class;)V",
        false);
  }
}
