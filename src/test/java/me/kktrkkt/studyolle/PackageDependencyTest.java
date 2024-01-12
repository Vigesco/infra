package me.kktrkkt.studyolle;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTest {

    private static final String ACCOUNT = "..modules.account..";
    private static final String STUDY = "..modules.study..";
    private static final String TOPIC = "..modules.topic..";
    private static final String ZONE = "..modules.zone..";
    private static final String EVENT = "..modules.event..";
    private static final String NOTIFICATION = "..modules.notification..";

    @ArchTest
    private ArchRule accountPackageRule = classes().that().resideInAnyPackage(ACCOUNT)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(ACCOUNT, STUDY, EVENT, NOTIFICATION);

    @ArchTest
    private ArchRule studyPackageRule = classes().that().resideInAnyPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT);

    @ArchTest
    private ArchRule topicPackageRule = classes().that().resideInAnyPackage(TOPIC)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(TOPIC, ACCOUNT, STUDY);

    @ArchTest
    private ArchRule zonePackageRule = classes().that().resideInAnyPackage(ZONE)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(ZONE, ACCOUNT, STUDY);

    @ArchTest
    private ArchRule eventPackageRule = classes().that().resideInAnyPackage(EVENT)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(EVENT);

    @ArchTest
    private ArchRule notificationPackageRule = classes().that().resideInAnyPackage(NOTIFICATION)
            .should().accessClassesThat().resideInAnyPackage(NOTIFICATION, ACCOUNT);

    @ArchTest
    private ArchRule freeOfCycles = slices().matching("me.kktrkkt.studyolle.modules.(*)..")
            .should().beFreeOfCycles();
}
