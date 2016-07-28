node {
    stage 'Checkout'

    git url: 'https://github.com/t1/rest-client.git'

    def mvnHome = tool 'M3'

    stage 'Package'
    sh "${mvnHome}/bin/mvn -Dmaven.test.failure.ignore clean package"
    step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}
