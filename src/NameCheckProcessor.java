import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author luosizhi
 * @ClassName NameCheckProcessor
 * @Function 注解处理器
 * @Date: 2019/2/28 19:59
 * @since JDK 1.7
 */

/**
 * 支持所有的注解
 * 只支持jdk8的代码
 * @author lsz
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class NameCheckProcessor extends AbstractProcessor {

    private NameChecker nameChecker;

    /**
     * 初始化名称检查插件
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        nameChecker = new NameChecker(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()){
            for (Element element : roundEnv.getRootElements()){
                nameChecker.checkNames(element);
            }
        }
        return false;
    }
}
