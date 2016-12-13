
import java.io.File
import sbt.Keys._
import sbt._

licenses in GlobalScope += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")

updateOptions := updateOptions.value.withCachedResolution(true)

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

resolvers := {
  val previous = resolvers.value
  if (git.gitUncommittedChanges.value)
    Seq[Resolver](Resolver.mavenLocal) ++ previous
  else
    previous
}

lazy val imce_metrology_isoiec80000_magicdraw_library =
  Project("imce-metrology-isoiec80000-magicdraw-library", file("."))
  .enablePlugins(AetherPlugin)
  .enablePlugins(GitVersioning)
  .enablePlugins(UniversalPlugin)
  .settings(dynamicScriptsResourceSettings("imce.metrology.isoiec80000.magicdraw.library"))
  .settings(
    projectID := {
      val previous = projectID.value
      previous.extra(
        "artifact.kind" -> "magicdraw.resource.library")
    },

    // disable automatic dependency on the Scala library
    autoScalaLibrary := false,

    // disable using the Scala version in output paths and artifacts
    crossPaths := false,

    publishMavenStyle := true,

    // do not include all repositories in the POM
    pomAllRepositories := false,

    // make sure no repositories show up in the POM file
    pomIncludeRepository := { _ => false },

    // disable publishing the main jar produced by `package`
    publishArtifact in(Compile, packageBin) := false,

    // disable publishing the main API jar
    publishArtifact in(Compile, packageDoc) := false,

    // disable publishing the main sources jar
    publishArtifact in(Compile, packageSrc) := false,

    // disable publishing the jar produced by `test:package`
    publishArtifact in(Test, packageBin) := false,

    // disable publishing the test API jar
    publishArtifact in(Test, packageDoc) := false,

    // disable publishing the test sources jar
    publishArtifact in(Test, packageSrc) := false,

    sourceGenerators in Compile := Seq(),

    managedSources in Compile := Seq(),

    resolvers += Resolver.bintrayRepo("tiwg", "org.omg.tiwg.vendor.nomagic"),

    libraryDependencies +=
      "org.omg.tiwg.vendor.nomagic"
        % "com.nomagic.magicdraw.package"
        % "18.0-sp6.+"
        artifacts
        Artifact(
          name="com.nomagic.magicdraw.package",
          `type`="pom",
          extension="pom")
  )

def dynamicScriptsResourceSettings(projectName: String): Seq[Setting[_]] = {

  import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._

  def addIfExists(f: File, name: String): Seq[(File, String)] =
    if (!f.exists) Seq()
    else Seq((f, name))

  val QUALIFIED_NAME = "^[a-zA-Z][\\w_]*(\\.[a-zA-Z][\\w_]*)*$".r

  Seq(
    // the '*-resource.zip' archive will start from: 'dynamicScripts/<dynamicScriptsProjectName>'
    com.typesafe.sbt.packager.Keys.topLevelDirectory in Universal := None,

    // name the '*-resource.zip' in the same way as other artifacts
    com.typesafe.sbt.packager.Keys.packageName in Universal :=
      normalizedName.value + "_" + scalaBinaryVersion.value + "-" + version.value + "-resource",

    // contents of the '*-resource.zip' to be produced by 'universal:packageBin'
    mappings in Universal in packageBin ++= {

      val modelLibraryFiles =
      (baseDirectory.value / "modelLibrary" / "IMCE" * "*.mdzip").pair(relativeTo(baseDirectory.value)).sortBy(_._2)

      val sampleFiles =
        (baseDirectory.value / "samples" / "IMCE" * "*.mdzip").pair(relativeTo(baseDirectory.value)).sortBy(_._2)

      val root = baseDirectory.value / "target" / "md.package"
      IO.createDirectory(root)

      val s = streams.value
      val d = {
        import java.util.{ Date, TimeZone }
        val formatter = new java.text.SimpleDateFormat("yyyy-MM-dd-HH:mm")
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
        formatter.format(new Date)
      }

      val ver = version.value

      s.log.warn(s"\n*** top: $root")

      val resourceManager = root / "data" / "resourcemanager"
      IO.createDirectory(resourceManager)

      val resourceDescriptorFile = resourceManager / "MDR_Model_Library_gov_nasa_jpl_imce_metrology_isoiec_80000_magicdraw_library_77533_descriptor.xml"
      val resourceDescriptorInfo =
        <resourceDescriptor critical="false" date={d}
                            description="IMCE MagicDraw library of ISO/IEC 80000 Metrology"
                            group="IMCE Resource"
                            homePage="https://github.com/JPL-IMCE/gov.nasa.jpl.imce.metrology.isoiec80000.magicdraw.library"
                            id="77533"
                            mdVersionMax="higher"
                            mdVersionMin="18.0"
                            name="IMCE MagicDraw library of ISO/IEC 80000 Metrology"
                            product="IMCE MagicDraw library of ISO/IEC 80000 Metrology"
                            restartMagicdraw="false"
                            type="Model Library">
          <version human={ver} internal={ver} resource={ver + "0"}/>
          <provider email="nicolas.f.rouquette@jpl.nasa.gov"
                    homePage="https://github.com/NicolasRouquette"
                    name="IMCE"/>
          <edition>Reader</edition>
          <edition>Community</edition>
          <edition>Standard</edition>
          <edition>Professional Java</edition>
          <edition>Professional C++</edition>
          <edition>Professional C#</edition>
          <edition>Professional ArcStyler</edition>
          <edition>Professional EFFS ArcStyler</edition>
          <edition>OptimalJ</edition>
          <edition>Professional</edition>
          <edition>Architect</edition>
          <edition>Enterprise</edition>
          <installation>
            {modelLibraryFiles.map { case (_, path) =>
            <file
            from={path}
            to={path}>
            </file>
          }}
          </installation>
        </resourceDescriptor>

      xml.XML.save(
        filename = resourceDescriptorFile.getAbsolutePath,
        node = resourceDescriptorInfo,
        enc = "UTF-8")

      (root.*** --- root).pair(relativeTo(root)) ++ modelLibraryFiles ++ sampleFiles
    },

    artifacts += {
      val n = (name in Universal).value
      Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map())
    },
    packagedArtifacts += {
      val p = (packageBin in Universal).value
      val n = (name in Universal).value
      Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) -> p
    }
  )
}
