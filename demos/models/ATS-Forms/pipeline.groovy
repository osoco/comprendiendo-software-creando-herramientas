def compile() {
// optional depending on each case
//  sh 'cp gradle-local.properties.sample gradle-local.properties'

  def gradleFolder = tool 'gradle'
  sh "${gradleFolder}/bin/gradle clean compileJava compileGroovy"
}

def unitTests() {
// optional depending on each case
//  sh 'cp gradle-local.properties.sample gradle-local.properties'

  def gradleFolder = tool 'gradle'
  sh "${gradleFolder}/bin/gradle test -x integrationTest"
  step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/test/TEST-*.xml', allowEmptyResults: true])
}

// Disable this if the project doesn't have integration tests.
def integrationTests() {
// optional depending on each case
//  sh 'cp gradle-local.properties.sample gradle-local.properties'

  def gradleFolder = tool 'gradle'
  sh "${gradleFolder}/bin/gradle integrationTest -x test"
  step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/test/TEST-*.xml', allowEmptyResults: true])
}

def build() {
  def gradleFolder = tool 'gradle'
  sh "${gradleFolder}/bin/gradle build -x compileJava -x compileGroovy -x test -x integrationTest"
}

return this
