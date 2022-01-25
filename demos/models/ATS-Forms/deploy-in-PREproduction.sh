#!/bin/bash

AWS_PROFILE="bbva-ats-admin";
FORMSERVICE_CODE_BUCKET="contestia-pre-11-form-buckets-code";
TAG="$(grep version build.gradle | head -n 1 | awk -F"'" '{print $2}')";
#FORMSERVICE_CODE_BUCKET="pre-bats-form-functions";
FUNCTION_PREFIX="pre-42-form-";
#FUNCTION_PREFIX="pre-bats-form-";
#FUNCTION_PREFIX="";

   ./gradlew clean buildZip \
&& mv build/distributions/bbva-ats-forms-${TAG}.zip build/distributions/FormService.zip \
&& aws s3 --profile bbva-ats-admin cp build/distributions/FormService.zip s3://${FORMSERVICE_CODE_BUCKET}

if [ $? -eq 0 ]; then
for f in recover-entity recover-draft new-token generic-token email-token verify-email-token form-config draft submit create-or-update-form application-data-recover agent-form-recovery; do
    aws lambda --profile ${AWS_PROFILE} update-function-code --function-name ${FUNCTION_PREFIX}${f} --s3-bucket ${FORMSERVICE_CODE_BUCKET} --s3-key FormService.zip
    if [ $? -ne 0 ]; then
      break;
    fi
  done
fi
#
