CLASSES = HopscotchPersistent.class HopscotchResident.class PointerPair.class Constants.class IndexCreator.class InvertedIndex.class

all : $(CLASSES)
	java InvertedIndex

dist : clean $(CLASSES)
	jar cvfm assignment2-grp4.jar META-INF/Manifest *.java *.class

%.class : %.java
	javac $<

clean :
	-rm *.dat

dump : $(CLASSES)
	./dump
