hg add
hg qrefresh
cd .hg\patches\
hg add
hg commit -m %1
hg push
cd ..\..
mvn package
copy /Y target\xdat-1.jar ..\xnat_builder_1_6dev\plugin-resources\repository\xdat\jars\

