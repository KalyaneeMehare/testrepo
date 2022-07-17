def call(String repoUrl,String gitu,String sonarname,String sonarid,String branch) {
def choice=[]
node {
    choice = params["build"].split(",")
}
pipeline {
		agent {
		docker {
			image 'maven:3.8.1-jdk-8'
			args '-v /root/.m2:/root/.m2'
			args '-v /opt/:/opt/'
			}
			}

	parameters {
  extendedChoice description: '', multiSelectDelimiter: ',', name: 'build', quoteValue: false, saveJSONParameterToFile: false, type: 'PT_MULTI_SELECT', value: 'compile,build,junit-test,integration-test,sonarqube-integration,package,renaming-the-jar,backup', visibleItemCount: 8
}
//	parameters {
//        choice(
//            choices: ['true' , 'false'],
//            description: '',
//            name: 'REQUESTED_ACTION')
//    }
// 	environment {          
//         def p = 'params.build'
// 		xyz = p.split(',,');
// 	}	
// 	parameters {
//       checkboxParameter name:'my-checkbox', format:'JSON', uri:'https://raw.githubusercontent.com/samitkumarpatel/test0/main/checkbox.json'
// 		{
//     "CheckboxParameter": [
//         {
//           "key": "compile",
//           "value": "compile"
//         }
//     ]
// }
// 		}	
     
//       consider this is the structure of CheckBox in https://raw.githubusercontent.com/samitkumarpatel/test0/main/checkbox.json URI
     
	stages {
			stage("Checkout Code") {
               steps {
		       git branch: "${branch}",
	           credentialsId: "${gitu}",
                    url: "${repoUrl}"
               }
           }
			stage('compile') {
// 				String[] str ;
// 				str = env.{params.build}.split(',');
// 				for ( String str in [ 'compile,build,junit-test,integration-test,sonarqube-integration,packing,renaming-the-jar,backup' ]) {
				//for (compile in ['params.build']) {
				//for ( compile in [xyz] ) {
				when {
					expression {'compile' in choice }
				}
				steps {
				       sh 'mvn compile'
				}
		}
			stage('Build') {
				//for (build in [xyz]) {
				//for ( "compile" in ${str}) {
				when  {
					expression { 'build' in choice }
				}
				steps {
				      sh 'mvn clean install'
			}
		}
                        stage('junit-test') {
				when {
					expression { 'junit-test' in choice }
				}
                                steps {
                                       sh 'mvn test'
                        }
        	        post {
 	               always {
                	    junit 'target/*.xml'
         	      	 }
           	 }
 	    }
			stage('integration-testing') {
				when {
					expression { 'integration-test' in choice }
            }
				steps {
				      sh 'mvn test -Dtest=**/*IT.java'
			}
		}
		stage('sonarqube-integration') {
			when {
                expression { 'sonarqube-integration' in choice }
            }
				steps {
					withSonarQubeEnv(credentialsId: 'sonar-token', installationName: 'sonarqube'){
						sh 'java -jar jacococli.jar report target/jacoco.exec --classfiles target/  --xml target/report.xml'
				       sh 'mvn sonar:sonar \
                                                           -Dsonar.tests=src/test/ \
		                                           -Dsonar.junit.reportsPath=target/ \
			                                   -Dsonar.coverage.jacoco.xmlReportPaths=target/report.xml'
			}
		}
			}
			stage('package')
                             {
				     when {
                expression { 'package' in choice }
            }
                                steps
                                     {
                                       sh 'mvn package spring-boot:repackage'
                                     }
	                     }
			stage('renaming-the-jar') {
				when {
                expression { 'renaming-the-jar' in choice }
            }
				steps {
				      sh 'mkdir -p /opt/backup'
				      sh 'mv target/sampleapp-1.0.0-SNAPSHOT-LOCAL.jar target/java-jar.${BUILD_NUMBER}.jar'
			}
		}
			stage('backing-up-the-jar') {
				when {
                expression { 'backing-up-the-jar' in choice }
            }
				steps {
				      sh 'cp target/java-jar.${BUILD_NUMBER}.jar /opt/backup/'
			}
		}	
	}
}
}
