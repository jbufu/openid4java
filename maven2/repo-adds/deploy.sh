#! /bin/sh

# deployement with mvn deploy:deploy-file
# !! require explicit groupId, artifactId, version and packaging in the pom.xml (doesn't support inherit from parent) !!

LIB_DIR=../../lib/infocard/
REPO_ID=alchim.sf.net
REPO_URL=scp://alchim.sf.net/home/groups/a/al/alchim/htdocs/download/snapshots
DEPLOY_OPTS="-DrepositoryId=$REPO_ID -Durl=$REPO_URL -DuniqueVersion=false"

mvn deploy:deploy-file -DpomFile=higgins-parent-pom.xml -Dfile=higgins-parent-pom.xml $DEPLOY_OPTS
for LIB in higgins-configuration-api higgins-sts-api higgins-sts-common higgins-sts-server-token-handler higgins-sts-spi ; do
  echo $LIB
  mvn deploy:deploy-file -DpomFile=$LIB-pom.xml -Dfile=$LIB_DIR/$LIB.jar $DEPLOY_OPTS
done

