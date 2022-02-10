#!/bin/bash

cd packages || exit

passed_plugins=()
failed_plugins=()
skipped_plugins=()

set +e
set -o pipefail

for plugin_dir in */; do
    cd "$plugin_dir" || exit
    plugin=$(basename "$plugin_dir")
    case $1 in
        flutter-test)
            echo "=== Running Flutter unit tests for $plugin ==="

            APP_FACING_PACKAGE_DIR=$(echo "${plugin}" |  sed 's/_plugin//g')
            RELATIVE_PATH_TO_PROJ_ROOT="../.."
            PLUGIN_NAME=$plugin

            if [ -d "$APP_FACING_PACKAGE_DIR" ]; then
                cd $APP_FACING_PACKAGE_DIR
                RELATIVE_PATH_TO_PROJ_ROOT="../../.."
                PLUGIN_NAME=$APP_FACING_PACKAGE_DIR
            fi

            if [ -d "test" ]; then
                mkdir -p test-results
                if flutter test --machine --coverage | tojunit --output "test-results/$plugin-flutter-test.xml"; then
                    echo "PASSED: Flutter unit tests for $plugin passed."
                    passed_plugins+=("$plugin")
                else
                    echo "FAILED: Flutter unit tests for $plugin failed."
                    failed_plugins+=("$plugin")
                fi
            else
                echo "SKIPPED: Flutter unit tests for $plugin don't exist. Skipping."
                skipped_plugins+=("$plugin")
            fi
            ;;
        android-test)
            echo "=== Running Android unit tests for $plugin ==="

            ANDROID_PLUGIN_DIR=$(echo "${plugin}_android" |  sed 's/_plugin//g')
            RELATIVE_PATH_TO_PROJ_ROOT="../.."
            PLUGIN_NAME=$plugin

            if [ -d "$ANDROID_PLUGIN_DIR" ]; then
                cd $ANDROID_PLUGIN_DIR
                RELATIVE_PATH_TO_PROJ_ROOT="../../.."
                PLUGIN_NAME=$ANDROID_PLUGIN_DIR
            fi

            if [ -d "android/src/test" ]; then
                if [ ! -d "example/android" ]; then
                    echo "FAILED: example/android missing, can't run tests."
                    failed_plugins+=("$PLUGIN_NAME")
                    continue
                fi
    
                cp ${RELATIVE_PATH_TO_PROJ_ROOT}/.circleci/dummy_amplifyconfiguration.dart example/lib/amplifyconfiguration.dart
                cd example/android
                if ./gradlew :"$PLUGIN_NAME":testDebugUnitTest; then
                    echo "PASSED: Android unit tests for $PLUGIN_NAME passed."
                    passed_plugins+=("$PLUGIN_NAME")
                    # if ./gradlew :"$plugin":testDebugUnitTestCoverage; then
                    #     echo "PASSED: Generating android unit tests coverage for $plugin passed."
                    #     passed_plugins+=("$plugin")
                    # else
                    #     echo "FAILED: Generating android unit tests coverage for $plugin failed."
                    #     failed_plugins+=("$plugin")
                    # fi
                else
                    echo "FAILED: Android unit tests for $PLUGIN_NAME failed."
                    failed_plugins+=("$PLUGIN_NAME")
                fi
                cd ${RELATIVE_PATH_TO_PROJ_ROOT}

            else
                echo "SKIPPED: Android unit tests for $PLUGIN_NAME don't exist. Skipping."
                skipped_plugins+=("$PLUGIN_NAME")
            fi
            ;;
        ios-test)
            echo "=== Running iOS unit tests for $plugin ==="

            IOS_PLUGIN_DIR=$(echo "${plugin}_ios" |  sed 's/_plugin//g')
            RELATIVE_PATH_TO_PROJ_ROOT="../.."
            PLUGIN_NAME=$plugin

            if [ -d "$IOS_PLUGIN_DIR" ]; then
                cd $IOS_PLUGIN_DIR
                RELATIVE_PATH_TO_PROJ_ROOT="../../.."
                PLUGIN_NAME=$IOS_PLUGIN_DIR
            fi

            if [ -d "example/ios/unit_tests" ]; then
                XCODEBUILD_DESTINATION="platform=iOS Simulator,name=iPhone 12"
                cp ${RELATIVE_PATH_TO_PROJ_ROOT}/.circleci/dummy_amplifyconfiguration.dart example/lib/amplifyconfiguration.dart
                cd example/ios
                if xcodebuild test \
                        -workspace Runner.xcworkspace \
                        -scheme Runner \
                        -destination "$XCODEBUILD_DESTINATION" | xcpretty \
                        -r "junit" \
                        -o "test-results/$IOS_PLUGIN_DIR-xcodebuild-test.xml"; then
                    echo "PASSED: iOS unit tests for $IOS_PLUGIN_DIR passed."
                    passed_plugins+=("$IOS_PLUGIN_DIR")
                else
                    echo "FAILED: iOS unit tests for $IOS_PLUGIN_DIR failed."
                    failed_plugins+=("$IOS_PLUGIN_DIR")
                fi
                cd ${RELATIVE_PATH_TO_PROJ_ROOT}
            else
                echo "SKIPPED: iOS unit tests for $plugin don't exist. Skipping."
                skipped_plugins+=("$plugin")
            fi
            ;;
    esac
    cd ..
    echo
done

echo "=== Unit test complete ==="
echo

echo "${#passed_plugins[@]} passed plugins:"
printf "* %s\n" "${passed_plugins[@]}"
echo

echo "${#failed_plugins[@]} failed plugins:"
printf "* %s\n" "${failed_plugins[@]}"
echo

echo "${#skipped_plugins[@]} skipped plugins:"
printf "* %s\n" "${skipped_plugins[@]}"
echo

cd ..

set -e

exit ${#failed_plugins[@]}
