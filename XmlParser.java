import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;
 
public class XmlParser implements Constants{

 	private static Map<String,List<PointerPair>> hm = new HashMap<String,List<PointerPair>>();
		
	public XmlParser(String xmlFile) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(xmlFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
 
		NodeList nList = doc.getElementsByTagName("MedlineCitation");
		for (int temp = 0; temp < nList.getLength(); temp++) {
 
		   Node nNode = nList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
		      	Element eElement = (Element) nNode;
 
		      	String ID = getTagValue("PMID", eElement);
			int pmid = new Integer(ID).intValue();		      
			
			String articleTitle = getTagValue("ArticleTitle",eElement);
			if(articleTitle!=null)
			{	
				tokenizeText(articleTitle, pmid, false);			
			}
			String articleText = getTagValue("AbstractText",eElement);			
			if (articleText!=null)
			{
				tokenizeText(articleText, pmid, true);	
			}
		}
		}
  }
 
  private static void tokenizeText(String text, int pmid, boolean inAbstract)
  {
	
	String[] list = text.split("");
	for( int i = 0; i < list.length-1; i++)
	{
		PointerPair p = new PointerPair(0,0);			
		if(inAbstract)
		{
			p.a = pmid;
			p.b = i|ABSTRACT_MASK;	
		}
		else
		{
			p.a = pmid;
			p.b = i;	
		}

		if(hm.containsKey(list[i]))
		{
			List<PointerPair> aux = hm.get(list[i].toLowerCase());
			aux.add(p);
			hm.put(list[i].toLowerCase(),aux);
		}
		else
		{
			ArrayList<PointerPair> aux = new ArrayList<PointerPair>();
			aux.add(p);
			hm.put(list[i].toLowerCase(),aux);
		}
	}
  }

  private static String getTagValue(String sTag, Element eElement) 
  {
	NodeList nlList = eElement.getElementsByTagName(sTag);
	if(nlList.getLength()>0)
	{
		nlList = nlList.item(0).getChildNodes();
	}	
	if(nlList.getLength()>0)
	{
        	Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
 	}
	else
	{
		return null;
	}		
  }
  
   public Map<String,List<PointerPair>> get ()
	{
	return hm;
	}



}
