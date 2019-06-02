package arch.de.eesit;

import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.junit.runner.RunWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;

import de.eesit.annotations.ValidatedMethod;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "..service..")
public class ValidatedServiceMethodTest {

	@ArchTest
	public static final ArchRule servicePostOrPutMethodsAnnotatedWithValidatedMethodShouldValidateAnInputParameter =
		classes().that().implement(interfaceWithPathAnot()).and()
				.implement(interfaceWithPostOrPutMethods()).and(methodsWithValdiatedMethodAnnotations())
				.should(haveValidAnnotatedParametersOnValidatedMethods());

	private static ArchCondition<JavaClass> haveValidAnnotatedParametersOnValidatedMethods() {
		return new ArchCondition<JavaClass>("have @Valid annotated parameter on validated methods") {

			@Override
			public void check(JavaClass item, ConditionEvents events) {
				allImplementedInterfaceMethods(item)
						.filter(methodAnnotatedWithValidatedMethod().and(methodParametersAreNotValidAnnotated()))
						.forEach(m -> events.add(violated(m, format("method %s", m.getFullName()))));
			}
		};
	}

	@SafeVarargs
	private static final boolean isMethodAnnotatedWithAnnotation(JavaMethod m, Class<? extends Annotation>... annotation) {
		return Stream.of(annotation).anyMatch(a -> m.isAnnotatedWith(a));
	}

	private static DescribedPredicate<JavaClass> methodsWithValdiatedMethodAnnotations() {
		return new DescribedPredicate<JavaClass>("have methods which are annotated with @ValidatedMethod") {

			@Override
			public boolean apply(JavaClass input) {
				return input.getMethods().stream().filter(m -> isMethodAnnotatedWithAnnotation(m, ValidatedMethod.class))
						.findAny().isPresent();
			}

		};
	}

	private static DescribedPredicate<JavaClass> interfaceWithPostOrPutMethods() {
		return new DescribedPredicate<JavaClass>("an interface with methods annotated with @POST or @PUT") {

			@Override
			public boolean apply(JavaClass input) {
				return input.getMethods().stream().filter(im -> isMethodAnnotatedWithAnnotation(im, POST.class, PUT.class))
						.findAny().isPresent();
			}

		};
	}

	private static DescribedPredicate<JavaClass> interfaceWithPathAnot() {
		return new DescribedPredicate<JavaClass>("an interface annotated with @Path") {
			@Override
			public boolean apply(JavaClass input) {
				return input.isAnnotatedWith(Path.class);
			}
		};
	}

	private static Predicate<JavaMethod> methodParametersAreNotValidAnnotated() {
		Predicate<JavaMethod> methodParametersAreNotValidAnnotated = iim -> !isParamaterOfMethodAnnotatedWithAnnotation(iim, Valid.class);
		return methodParametersAreNotValidAnnotated;
	}

	private static Predicate<JavaMethod> methodAnnotatedWithValidatedMethod() {
		Predicate<JavaMethod> methodAnnotatedWithValidatedMethod = iim -> isMethodAnnotatedWithAnnotation(iim, ValidatedMethod.class);
		return methodAnnotatedWithValidatedMethod;
	}

	private static Stream<JavaMethod> allImplementedInterfaceMethods(JavaClass item) {
		return allInterfaceMethods(item)
				.flatMap(iim -> item.getMethods().stream().filter(m -> methodsAreEqual(iim, m)));
	}

	private static Stream<JavaMethod> allInterfaceMethods(JavaClass item) {
		return item.getInterfaces().stream().flatMap(i -> i.getMethods().stream());
	}

	private static boolean isParamaterOfMethodAnnotatedWithAnnotation(JavaMethod m, Class<? extends Annotation> annotation) {
		return Stream.of(m.reflect().getParameterAnnotations()).flatMap(a -> Stream.of(a))
				.map(a -> a.annotationType()).filter(t -> t.equals(annotation)).findAny().isPresent();
	}

	private static boolean methodsAreEqual(JavaMethod im, JavaMethod m) {
		return m.getName().equals(im.getName()) && m.getRawParameterTypes().equals(im.getRawParameterTypes())
				&& m.getRawReturnType().equals(im.getRawReturnType());
	}
	
}
