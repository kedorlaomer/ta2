CLASSES = HopscotchPersistent.class HopscotchResident.class PointerPair.class Constants.class

all : $(CLASSES)
	java HopscotchPersistent

dist : clean $(CLASSES)
	jar cvfm assignment2-grp4.jar META-INF/Manifest *.java *.class

%.class : %.java
	javac $<

clean :
	rm *.dat
