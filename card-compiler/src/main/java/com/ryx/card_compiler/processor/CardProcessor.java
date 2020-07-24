package com.ryx.card_compiler.processor;

import com.google.auto.service.AutoService;

import com.ryx.card_annotation.annotation.Card;
import com.ryx.card_annotation.model.CardMeta;
import com.ryx.card_compiler.utils.Consts;
import com.ryx.card_compiler.utils.Logger;
import com.ryx.card_compiler.utils.TypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


import static com.ryx.card_compiler.utils.Consts.ANNOTATION_ROUTER_NAME;
import static com.ryx.card_compiler.utils.Consts.ICARD_GROUP;
import static com.ryx.card_compiler.utils.Consts.ICARD_ROOT;
import static com.ryx.card_compiler.utils.Consts.METHOD_LOAD_INTO;
import static com.ryx.card_compiler.utils.Consts.MODEL;
import static com.ryx.card_compiler.utils.Consts.NAME_OF_ROOT;
import static com.ryx.card_compiler.utils.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.ryx.card_compiler.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Created by hezhiqiang on 2018/11/21.
 * Router注解处理器
 */

@AutoService(Processor.class)       //自动注册注解处理器
@SupportedOptions({Consts.KEY_MODULE_NAME})     //参数
@SupportedSourceVersion(SourceVersion.RELEASE_7)        //指定使用的Java版本
@SupportedAnnotationTypes({ANNOTATION_ROUTER_NAME}) //指定要处理的注解类型
public class CardProcessor extends AbstractProcessor{

    private Map<String,Set<CardMeta>> groupMap = new HashMap<>();  //收集分组
    private Map<String,String> rootMap = new TreeMap<>();
    private Filer mFiler;
    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    private Elements elements;
    private String moduleName = "app"; //默认app
    private TypeMirror iProvider = null; //IProvider类型

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
        elements = processingEnvironment.getElementUtils();

        typeUtils = new TypeUtils(types,elements);
        logger = new Logger(processingEnvironment.getMessager()); //log工具
        Map<String, String> options = processingEnvironment.getOptions();

        if(MapUtils.isNotEmpty(options)&&options.containsKey(Consts.KEY_MODULE_NAME)) {
            moduleName = options.get(Consts.KEY_MODULE_NAME);
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
        }else{
            logger.error("NO moduleName");
            throw new RuntimeException("CardManager::Compiler >>> No module name, for more information, look at gradle log.");
        }
        logger.warning(">>> Found routers,---------------------------init.. <.<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            logger.warning( moduleName+"并没有发现 被@ARouter注解的地方呀");
            return false;
        }


        // 获取所有被 @Card 注解的 元素集合
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(Card.class);
        try {
            logger.warning(">>> Found routers,start... <<<");
            parseCards(elementsAnnotatedWith);
        } catch (IOException e) {
            logger.error(e);
        }
        return true;
    }

    private void parseCards(Set<? extends Element> routeElements) throws IOException {


        if(!routeElements.isEmpty()) {

            logger.warning(">>> Found routes, size is " + routeElements.size() + " <<<");
            rootMap.clear();

            TypeMirror type_model = elements.getTypeElement(MODEL).asType();

            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                Card card = element.getAnnotation(Card.class);
                CardMeta cardMeta;

                if(types.isSubtype(tm,type_model)) { //model
                    logger.warning(">>> Found model route: "+ tm.toString() + " 111<<<");
                    cardMeta=new CardMeta.Builder()
                            .addElement(element)
                            .addGroup(card.group())
                            .addTypeName(card.typeName())
                            .addTypeEnum(CardMeta.TypeEnum.MODEL)
                            .addResId(card.resId())
                            .build();
                    logger.warning(">>> Found model route: "+ tm.toString() + " 222<<<");
                }  else {
                    throw new RuntimeException("ARouter::Compiler >>> Found unsupported class type, type = [" + types.toString() + "].");
                }

                categories(cardMeta);
            }
            //interface of route
            TypeElement type_ICardGroup = elements.getTypeElement(ICARD_GROUP);

            TypeElement type_ICardRoot = elements.getTypeElement(ICARD_ROOT);
            // TODO 第一大步：PAHT  一群小弟
            logger.warning("<<<<<<< 第一步>>>>>>>");
            try {
                createPathFile(type_ICardGroup);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("在生成PATH模板时，异常了 e:" + e.getMessage());
            }

            // TODO 第二大步：GROUP  group的仓库 + 1（path）   组头（带头大哥）
            logger.warning("<<<<<<< 第二步>>>>>>>");
            try {
                createGroupFile(type_ICardRoot, type_ICardGroup);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("在生成GROUP模板时，异常了 e:" + e.getMessage());
            }
            logger.warning("文件生成");

            return ; // 坑：必须写返回值，表示处理@ARouter注解完成


        }
    }

    /**
     * Sort metas in group.
     *
     * @param cardMeta metas.
     */
    private void categories(CardMeta cardMeta) {
        if (routeVerify(cardMeta)) {
            logger.info(">>> Start categories, group = " + cardMeta.getGroup() + ", typeName = " + cardMeta.getTypeName() + " <<<");
            Set<CardMeta> routeMetas = groupMap.get(cardMeta.getGroup());
            if (CollectionUtils.isEmpty(routeMetas)) {
                Set<CardMeta> routeMetaSet = new TreeSet<>(new Comparator<CardMeta>() {
                    @Override
                    public int compare(CardMeta r1, CardMeta r2) {
                        try {
                            return r1.getTypeName().compareTo(r2.getTypeName());
                        } catch (NullPointerException npe) {
                            logger.error(npe.getMessage());
                            return 0;
                        }
                    }
                });
                routeMetaSet.add(cardMeta);
                groupMap.put(cardMeta.getGroup(), routeMetaSet);
                logger.info(">>> end categories, group = " + cardMeta.getGroup() + ", typeName = " + cardMeta.getTypeName() + " <<<");
            } else {
                routeMetas.add(cardMeta);
            }
        } else {
            logger.warning(">>> Route meta verify error, group is " + cardMeta.getGroup() + " <<<");
        }
    }

    /**
     * Verify the route meta
     *
     * @param meta raw meta
     */
    private boolean routeVerify(CardMeta meta) {
        String typeName = meta.getTypeName();

        if (StringUtils.isEmpty(typeName)) {   // The path must be start with '/' and not empty!
            return false;
        }

        if (StringUtils.isEmpty(meta.getGroup())) { // Use default group(the first word in path)
            try {
                String defaultGroup =moduleName;
                if (StringUtils.isEmpty(defaultGroup)) {
                    return false;
                }

                meta.setGroup(defaultGroup);
                return true;
            } catch (Exception e) {
                logger.error("Failed to extract default group! " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * TODO　PATH　生成
     * @param pathType
     * @throws IOException
     */
    private final void createPathFile(TypeElement pathType) throws IOException {
        // 判断 map仓库中，是否有需要生成的文件
        if (groupMap.isEmpty()) {
            return;
        }

        // Map<String, RouterBean>  返回值
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class), // Map
                ClassName.get(String.class), // Map<String,
                ClassName.get(CardMeta.class) // Map<String, CardMeta>
        );

        for (Map.Entry<String, Set<CardMeta>> entry : groupMap.entrySet()) {
            // 1.方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getPathMap")
                    .addAnnotation(Override.class) // 给方法上添加注解
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .returns(methodReturn)
                    ;
            //  Map<String, RouterBean> pathMap = new HashMap<>();
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class), // Map
                    ClassName.get(String.class), // String
                    ClassName.get(CardMeta.class), // RouterBean
                    "pathMap",
                    ClassName.get(HashMap.class)
            );

            Set<CardMeta> pathList = entry.getValue();

            for (CardMeta routerBean : pathList) {
                /**
                 * pathMap.put("/personal/Personal_MainActivity",
                 *                 CardMeta create(TypeEnum typeEnum,
                 *                 String typeName,
                 *                 Class<?> clazz,
                 *                 Class<?> presenter,
                 *                 int resId,
                 *                 String group)
                 *             );
                 */
                // 给方法添加代码
                methodBuilder.addStatement(
                        "$N.put($S, $T.create($T.$L,$S, $T.class, $L, $S))",
                        "pathMap", // x
                        routerBean.getTypeName(), // "/app/MainActivity"
                        ClassName.get(CardMeta.class), // RouterBean
                        ClassName.get(CardMeta.TypeEnum.class), // RouterBean.Type
                        routerBean.getTypeEnum(), // 枚举类型：ACTIVITY
                        routerBean.getTypeName(),
                        ClassName.get((TypeElement) routerBean.getElement()), // model.class
                     //   ClassName.get(routerBean.getPresenter()), // presenter.class
                        routerBean.getResId(), // 资源数据id
                        routerBean.getGroup() // 组名
                );
            } // for end

            //  return pathMap;
            methodBuilder.addStatement("return $N", "pathMap");

            // 最终生成的类文件名  ARouter$$Path$$  + personal
            String finalClassName = Consts.NAME_OF_GROUP + entry.getKey();

            // 生成 和 类 等等，结合一体
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE, // 包名
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(pathType)) // 实现ARouterLoadPath接口
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(mFiler); // 文件生成器开始生成类文件

            // 告诉Group
            rootMap.put(entry.getKey(), finalClassName);

            // PATH 全部结束
        }
    }

    /**
     * TODO GROUP 生成
     * @param groupType
     * @param pathType
     * @throws IOException
     */
    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {

        // 判断是否有需要生成的类文件
        if(rootMap.isEmpty()||groupMap.isEmpty())
         return;

        // Map<String, Class<? extends ARouterPath>>  返回参数
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class), // Map
                ClassName.get(String.class), // Map<String,
                // 第二个参数：Class<? extends ARouterLoadPath>
                // 某某Class是否属于ARouterLoadPath接口的实现类
                // <>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))));

        // 方法架子
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder("getGroupMap") // 方法名
                .addAnnotation(Override.class) // 重写注解
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(methodReturns); // 方法返回值

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                "groupMap",
                HashMap.class);

        // 方法内容配置
        for (Map.Entry<String, String> entry : rootMap.entrySet()) {
            // groupMap.put("personal", ARouter$$Path$$personal.class);
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    "groupMap", // groupMap.put
                    entry.getKey(),
                    // 类文件在指定包名下
                    ClassName.get(PACKAGE_OF_GENERATE_FILE, entry.getValue()));
        }

        // 遍历之后：return groupMap;
        methodBuidler.addStatement("return $N", "groupMap");

        // 最终生成的类文件名   ARouter$$Group$$ + personal
        String finalClassName = NAME_OF_ROOT + moduleName;
        logger.info("APT生成路由组Group类文件：" +
                PACKAGE_OF_GENERATE_FILE + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        JavaFile.builder(PACKAGE_OF_GENERATE_FILE, // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(groupType)) // 实现ARouterLoadGroup接口
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(mFiler); // 文件生成器开始生成类文件
    }
}
