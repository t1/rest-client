#!groovy

String mvn(args) {
    return sh(returnStdout: true, script: "${tool 'maven-3.3.x'}/bin/mvn ${args}")
}

node {
    stage('Checkout') {
        checkout scm
    }

    stage('Build') {
        mvn '-Dmaven.test.failure.ignore clean install'
        step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
        step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
    }

    stage('Release') {
        input message: "Ready for release"
//        mvn 'release:prepare release:perform --batch-mode'
//        sh 'git push'
    }
}
