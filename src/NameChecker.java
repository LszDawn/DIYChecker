import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementScanner8;
import javax.tools.Diagnostic;
import java.util.EnumSet;

/**
 * @author luosizhi
 * @ClassName NameChecker
 * @Function 命名检查器, 如果程序命名不合规范，将输出一个WARNING信息
 * @Date: 2019/2/28 20:02
 * @since JDK 1.7
 */
public class NameChecker {

    private final Messager messager;

    NameCheckScanner nameCheckScanner = new NameCheckScanner();

    public NameChecker(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
    }

    public void checkNames(Element element) {
        nameCheckScanner.scan(element);
    }

    /**
     * 名称检查器实现类，将会以Visitor模式访问抽象语法树中的元素
     */
    private class NameCheckScanner extends ElementScanner8<Void, Void> {

        /**
         * 检查变量
         *
         * @param e
         * @param aVoid
         * @return
         */
        @Override
        public Void visitVariable(VariableElement e, Void aVoid) {
            //如果这个变量是枚举或者常量，则按大写命名检查，否则按驼峰检查
            if (e.getKind() == ElementKind.ENUM_CONSTANT || e.getConstantValue() != null || heuristicallyConstant(e)){
                checkAllCaps(e);
            }else {
                checkCamelCase(e, false);
            }
            return null;
        }

        private boolean heuristicallyConstant(VariableElement e) {
            if (e.getEnclosingElement().getKind() == ElementKind.INTERFACE){
                return true;
            }else if(e.getKind() == ElementKind.FIELD && e.getModifiers().containsAll(EnumSet.of(Modifier.PUBLIC,
                    Modifier.STATIC, Modifier.FINAL))){
                return true;
            }else {
                return false;
            }
        }

        /**
         * 检查Java类
         *
         * @param e
         * @param aVoid
         * @return
         */
        @Override
        public Void visitType(TypeElement e, Void aVoid) {
            scan(e.getTypeParameters(), aVoid);
            checkCamelCase(e, true);
            super.visitType(e, aVoid);
            return null;
        }

        /**
         * 驼峰命名检查
         *
         * @param e
         * @param initialCaps
         */
        private void checkCamelCase(Element e, boolean initialCaps) {
            String name = e.getSimpleName().toString();
            boolean previousUpper = false;
            boolean conventional = true;
            int firstCodePoint = name.codePointAt(0);
            if (Character.isUpperCase(firstCodePoint)) {
                previousUpper = true;
                if (!initialCaps) {
                    messager.printMessage(Diagnostic.Kind.WARNING, "名称" + name + "应当以小写字母开头", e);
                    return;
                }
            } else {
                conventional = false;
            }
            if (conventional) {
                int cp = firstCodePoint;
                for (int i = Character.charCount(cp); i < name.length(); i += Character.charCount(cp)) {
                    cp = name.codePointAt(i);
                    if (Character.isUpperCase(cp)) {
                        if (previousUpper) {
                            conventional = false;
                            break;
                        }
                        previousUpper = true;
                    } else {
                        previousUpper = false;
                    }
                }
                if (!conventional) {
                    messager.printMessage(Diagnostic.Kind.WARNING, "名称" + name + "不符合驼峰命名法", e);
                }
            }
        }


        private void checkAllCaps(Element e) {
            String name = e.getSimpleName().toString();
            boolean conventional = true;
            int firstCodePoint = name.codePointAt(0);

            if (!Character.isUpperCase(firstCodePoint)) {
                conventional = false;
            } else {
                boolean previousUnderscore = false;
                int cp = firstCodePoint;
                for (int i = Character.charCount(cp); i < name.length(); i += Character.charCount(cp)) {
                    cp = name.codePointAt(i);
                    if (cp == (int) '_') {
                        if (previousUnderscore) {
                            conventional = false;
                            break;
                        }
                        previousUnderscore = true;
                    }else {
                        previousUnderscore = false;
                        if (!Character.isUpperCase(cp) && Character.isDigit(cp)){
                            conventional = false;
                            break;
                        }
                    }
                }
            }
            if (!conventional){
                messager.printMessage(Diagnostic.Kind.WARNING, "常量" + name + "应当全部以大写字母或下划线命名,并且以字母开头", e);
            }
        }

        /**
         * 检查方法
         *
         * @param e
         * @param aVoid
         * @return
         */
        @Override
        public Void visitExecutable(ExecutableElement e, Void aVoid) {
            if (e.getKind() == ElementKind.METHOD){
                Name name = e.getSimpleName();
                if (name.contentEquals(e.getEnclosingElement().getSimpleName())){
                    messager.printMessage(Diagnostic.Kind.WARNING, "一个普通方法" + name + "应当避免与类名重复以避免与构造函数产生混淆", e);
                    checkCamelCase(e, false);
                    super.visitExecutable(e, aVoid);
                }
            }
            return null;
        }
    }
}
