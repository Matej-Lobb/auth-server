pipeline {
  agent any
  tools { 
    maven 'Maven 3.6.1' 
  }
  environment {
    CODACY_PROJECT_TOKEN = '1343680a8be749e799f91f1297d359ed'
    CODECOV_TOKEN = 'ea8c7c98-fb26-441c-b9bc-b767ff8a7deb'
  }
  stages {
    stage('Initialize') {
      steps {
        sh '''
          echo "$PATH"
          java -version
          echo $JAVA_VERSION
          mvn -version
          '''
      }
    }
    stage('Build') {
      steps {
        sh 'mvn clean install jacoco:report'
      }
    }
    stage('Sonar') {
      steps {
        sh 'mvn sonar:sonar -Dsonar.host.url=https://sonar.mlobb.sk'
      }
    }
    stage('Codacy') {
      steps {
        sh '''
        wget -O codacy-coverage-reporter https://github.com/codacy/codacy-coverage-reporter/releases/download/6.0.4/codacy-coverage-reporter-linux-6.0.4
        chmod +x codacy-coverage-reporter
        ./codacy-coverage-reporter report -l Java -r target/site/jacoco/jacoco.xml
        '''
      }
    }
    stage('Codecov') {
      steps {
        sh 'curl -s https://codecov.io/bash | bash -s'
      }
    }
    stage('Remove Docker Image') {
      when {
        branch 'develop' 
      }
      steps {
        sh '''
            docker-compose -f docker-compose-db.yml down --remove-orphans || true
        '''
      }
    }
    stage('Build and Publish Docker Image') {
      when {
        branch 'develop' 
      }
      steps {
        sh '''
            docker-compose -f docker-compose-db.yml up -d --build
        '''
      }
    }
  }
}
