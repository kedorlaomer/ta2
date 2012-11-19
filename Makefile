CLASSES = HopscotchPersistent.class HopscotchResident.class PointerPair.class Constants.class IndexCreator.class InvertedIndex.class XmlParser.class

all : $(CLASSES)
	java InvertedIndex test_08n0147.xml

dist : clean $(CLASSES)
	jar cvfm assignment2-grp4.jar META-INF/Manifest *.java *.class

%.class : %.java
	javac $<

clean :
	-rm *.dat

dump : $(CLASSES)
	./dump
