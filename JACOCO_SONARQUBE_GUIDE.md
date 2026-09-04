# JaCoCo Code Coverage and SonarQube Integration Guide

## Overview

This project uses JaCoCo (Java Code Coverage) to measure test coverage and generate reports that can be consumed by SonarQube for code quality analysis.

## JaCoCo Configuration

### Maven Configuration

JaCoCo has been configured in the parent `pom.xml` with the following settings:

```xml
<properties>
    <jacoco.version>0.8.11</jacoco.version>
    <sonar.java.coveragePlugin>jacoco</sonar.java.coveragePlugin>
    <sonar.dynamicAnalysis>reuseReports</sonar.dynamicAnalysis>
    <sonar.coverage.jacoco.xmlReportPaths>${project.basedir}/../target/site/jacoco-aggregate/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
    <sonar.language>java</sonar.language>
</properties>
```

### Plugin Executions

JaCoCo runs automatically during the Maven lifecycle:

1. **prepare-agent**: Instruments the code before tests run
2. **report**: Generates coverage reports after tests complete
3. **check**: Validates coverage meets minimum thresholds (currently set to 0% to not fail builds)

## Generating Coverage Reports

### Run Tests with Coverage

```bash
# Run tests for all modules
mvn clean test

# Run tests for a specific module
mvn clean test -pl payment-service

# Run tests and skip coverage
mvn clean test -Djacoco.skip=true
```

### Generate Coverage Reports Only

```bash
# Generate reports without running tests again
mvn jacoco:report
```

### Package with Coverage

```bash
# Run full build with tests and coverage
mvn clean package
```

## Coverage Report Locations

After running tests, coverage reports are generated in each module:

```
<module>/target/site/jacoco/
├── index.html          # HTML coverage report (open in browser)
├── jacoco.xml          # XML report for SonarQube
├── jacoco.csv          # CSV report for spreadsheets
└── <package>/          # Package-level reports
```

### Example Locations

- **Auth Service**: `auth-service/target/site/jacoco/index.html`
- **Event Service**: `event-service/target/site/jacoco/index.html`
- **Ticket Service**: `ticket-service/target/site/jacoco/index.html`
- **Payment Service**: `payment-service/target/site/jacoco/index.html`

## Viewing Coverage Reports

### HTML Reports

Open the HTML report in your browser:

```bash
# macOS
open payment-service/target/site/jacoco/index.html

# Linux
xdg-open payment-service/target/site/jacoco/index.html

# Windows
start payment-service/target/site/jacoco/index.html
```

The HTML report shows:
- Overall coverage percentages
- Line coverage
- Branch coverage
- Method coverage
- Class coverage
- Package-level breakdowns

### Coverage Metrics

JaCoCo tracks several coverage metrics:

- **Line Coverage**: Percentage of code lines executed
- **Branch Coverage**: Percentage of decision branches taken
- **Instruction Coverage**: Percentage of bytecode instructions executed
- **Complexity Coverage**: Cyclomatic complexity coverage
- **Method Coverage**: Percentage of methods invoked
- **Class Coverage**: Percentage of classes loaded

## SonarQube Integration

### Prerequisites

1. **SonarQube Server**: Running instance (local or cloud)
2. **SonarQube Scanner**: Maven plugin or standalone scanner
3. **Project Token**: Authentication token from SonarQube

### Configure SonarQube Properties

Create a `sonar-project.properties` file in the project root:

```properties
# Project identification
sonar.projectKey=event-ticket-booking-system
sonar.projectName=Event Ticket Booking System
sonar.projectVersion=1.0.0-SNAPSHOT

# Source and test directories
sonar.sources=src/main/java
sonar.tests=src/test/java

# Java version
sonar.java.source=17
sonar.java.target=17

# Coverage reports
sonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml
sonar.java.coveragePlugin=jacoco

# Encoding
sonar.sourceEncoding=UTF-8

# Exclusions (optional)
sonar.exclusions=**/target/**,**/generated/**
sonar.test.exclusions=**/target/**
```

### Run SonarQube Analysis

#### Using Maven Plugin

Add SonarQube properties to your Maven command:

```bash
# With SonarQube Cloud
mvn clean verify sonar:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=your-org \
  -Dsonar.login=your-token

# With local SonarQube
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

#### Using Standalone Scanner

```bash
# Run tests and generate coverage
mvn clean test

# Run SonarQube scanner
sonar-scanner \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

### CI/CD Integration

#### GitHub Actions Example

```yaml
name: SonarQube Analysis

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Build and analyze
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          mvn clean verify sonar:sonar \
            -Dsonar.host.url=https://sonarcloud.io \
            -Dsonar.organization=${{ secrets.SONAR_ORGANIZATION }} \
            -Dsonar.projectKey=event-ticket-booking-system
```

#### GitLab CI Example

```yaml
sonarqube:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  variables:
    SONAR_USER_HOME: "${CI_PROJECT_DIR}/.sonar"
  cache:
    key: "${CI_JOB_NAME}"
    paths:
      - .sonar/cache
      - .m2/repository
  script:
    - mvn clean verify sonar:sonar
      -Dsonar.host.url=$SONAR_HOST_URL
      -Dsonar.login=$SONAR_TOKEN
  only:
    - main
    - develop
```

## Coverage Thresholds

### Current Configuration

The project is configured with a minimum coverage threshold of 0% to not fail builds. This can be adjusted in the parent `pom.xml`:

```xml
<execution>
    <id>jacoco-check</id>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>PACKAGE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.0</minimum>  <!-- Change this value -->
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

### Recommended Thresholds

For production projects, consider these thresholds:

- **Minimum**: 60% line coverage
- **Good**: 70-80% line coverage
- **Excellent**: 80%+ line coverage

### Setting Module-Specific Thresholds

You can override thresholds in individual module `pom.xml` files:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.60</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Excluding Code from Coverage

### Using Annotations

Exclude specific methods or classes:

```java
import lombok.Generated;

@Generated  // Lombok-generated code
public class MyDto {
    // ...
}
```

### Using Configuration

Exclude packages or classes in `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/entity/**</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/*Application.class</exclude>
        </excludes>
    </configuration>
</plugin>
```

## Current Test Coverage

As of the latest run:

| Module | Tests Run | Status |
|--------|-----------|--------|
| Auth Service | 29 | ✅ All Passing |
| Event Service | 43 | ✅ All Passing |
| Ticket Service | 48 | ✅ All Passing |
| Payment Service | 0 | ⚠️ No tests executed |
| Notification Service | 0 | ⚠️ No tests |

## Troubleshooting

### No Coverage Data Generated

**Issue**: "Skipping JaCoCo execution due to missing execution data file"

**Solutions**:
1. Ensure tests are actually running: `mvn test`
2. Check that JaCoCo agent is attached: Look for "argLine set to -javaagent" in logs
3. Verify test execution: Check surefire reports in `target/surefire-reports/`

### Coverage Report Not Found

**Issue**: SonarQube can't find coverage reports

**Solutions**:
1. Verify XML report exists: `find . -name "jacoco.xml"`
2. Check SonarQube property: `sonar.coverage.jacoco.xmlReportPaths`
3. Use absolute paths or wildcards: `**/target/site/jacoco/jacoco.xml`

### Tests Not Running

**Issue**: Maven Surefire not finding tests

**Solutions**:
1. Check test naming: Tests should end with `Test`, `Tests`, or `TestCase`
2. Verify test directory: `src/test/java`
3. Check Surefire plugin version in parent POM
4. Add `-X` flag for debug output: `mvn test -X`

### Low Coverage Numbers

**Issue**: Coverage is lower than expected

**Solutions**:
1. Write more unit tests for business logic
2. Focus on critical paths first
3. Use coverage reports to identify untested code
4. Consider integration tests for complex scenarios

## Best Practices

### Writing Testable Code

1. **Use Dependency Injection**: Makes mocking easier
2. **Keep Methods Small**: Easier to test individual units
3. **Avoid Static Methods**: Difficult to mock
4. **Separate Concerns**: Business logic vs infrastructure

### Test Coverage Strategy

1. **Focus on Business Logic**: Prioritize service layer tests
2. **Test Edge Cases**: Null values, empty collections, boundaries
3. **Test Error Handling**: Exception scenarios
4. **Integration Tests**: For complex workflows

### Continuous Improvement

1. **Set Incremental Goals**: Gradually increase coverage
2. **Review Coverage Reports**: Identify gaps regularly
3. **Make Coverage Visible**: Display in CI/CD dashboards
4. **Don't Chase 100%**: Focus on meaningful tests

## Additional Resources

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [SonarQube Maven Plugin](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review Maven build logs with `-X` flag
3. Consult JaCoCo and SonarQube documentation
4. Check project-specific configuration in `pom.xml`
