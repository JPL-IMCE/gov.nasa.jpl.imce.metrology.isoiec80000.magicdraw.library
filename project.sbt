
sbtPlugin := false

name := "gov.nasa.jpl.imce.metrology.isoiec80000.magicdraw.library"

description := "IMCE MagicDraw library of ISO/IEC 80000 Metrology"

moduleName := name.value

organization := "gov.nasa.jpl.imce"

organizationName := "JPL-IMCE"

organizationHomepage := Some(url(s"https://github.com/${organizationName.value}"))

homepage := Some(url(s"https://jpl-imce.github.io/${moduleName.value}"))

git.remoteRepo := s"git@github.com:${organizationName.value}/${moduleName.value}"

// publish to bintray.com via: `sbt publish`
publishTo := Some(
  "JPL-IMCE" at
    s"https://api.bintray.com/content/jpl-imce/${organization.value}/${moduleName.value}/${version.value}")

scmInfo := Some(ScmInfo(
  browseUrl = url(s"https://github.com/${organizationName.value}/${moduleName.value}"),
  connection = "scm:"+git.remoteRepo.value))

developers := List(
  Developer(
    id="NicolasRouquette",
    name="Nicolas F. Rouquette",
    email="nicolas.f.rouquette@jpl.nasa.gov",
    url=url("https://github.com/NicolasRouquette")))

