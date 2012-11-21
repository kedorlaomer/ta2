CLASSES = HopscotchPersistent.class HopscotchResident.class PointerPair.class Constants.class IndexCreator.class InvertedIndex.class XmlParser.class FindWords.class WordFinder.class
SOURCES = $(CLASSES:.class=.java)

all : $(CLASSES)
	rlwrap jdb WordFinder reduce appeal

dist : clean $(CLASSES)
	jar cvfm assignment2-grp4.jar META-INF/Manifest $(CLASSES) $(SOURCES)

%.class : %.java
	javac -g $<

clean :
	-rm *.dat *.class

dump : $(CLASSES)
	./dump
