import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;
 
public class XmlParser implements Constants
{
	/* 
	* Die Datenstrucktur wo alles gespeichert wird
	*/
	private static Map<String,List<PointerPair>> hm = new HashMap<String,List<PointerPair>>();

	/*
	* Konstruktor liest eine XML Datei und 
	*/
	public XmlParser(String xmlFile) throws ParserConfigurationException, SAXException, IOException 
	{
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

				String[] ID = getTagValue("PMID", eElement);
				int pmid = new Integer(ID[0]).intValue();		      
                     
				
				String[] articleTitle = getTagValue("ArticleTitle",eElement);
				if(articleTitle!=null)
				{	
					tokenizeText(articleTitle, pmid, false);			
				}


				String[] articleText = getTagValue("AbstractText",eElement);			
				if (articleText!=null)
				{
					tokenizeText(articleText, pmid, true);	
				}
			}
		}
	}


	private static void tokenizeText(String[] texts, int pmid, boolean inAbstract)
        {
                //Wenn es Elemente zum tokenisieren
		if(texts.length>0)
                {
                        int cont = 0;
			for( int i = 0; i < texts.length; i++)
			{
				//Im Fall dass es mehrere Texte mit dem selben Tag gibt, dann tokenisieren wir alle, aber speichern eine Position mehr von Text zu Text, so das sie nicht nacheinander sind.
				if(cont>0)
					cont = cont + 2;
				if (texts[i]!=null)
				{
					String[] list = texts[i].split("\\s");
                        
                        		for(String roughWord : list)
                        		{
                                		if(roughWord.length()>0)
                                		{
                                        		String currentWord = roughWord.toLowerCase();
                                        		//System.out.println(currentWord + " at position " + cont);
                                        		//if (currentWord.equals("detecting"))
                                        		//{
                                                		//System.out.println("'detecting' detected in PMID " + new Integer(pmid).toString() +" at position " + new Integer(cont).toString());
                                        		//}
                                        		PointerPair p = new PointerPair(0,0);

                                        		if(inAbstract)
                                        		{
                                                		p.a = pmid;
                                                		p.b = cont|ABSTRACT_MASK;
                                        		}
                                        		else
                                        		{
                                                		p.a = pmid;
                                                		p.b = cont;
                                        		}

                                        		if(hm.containsKey(currentWord))
                                        		{
                                                		List<PointerPair> aux = hm.get(currentWord);
                                                		aux.add(p);
                                                		hm.put(currentWord,aux);
                                        		}
                                        		else
                                        		{
                                                		ArrayList<PointerPair> aux = new ArrayList<PointerPair>();
                                                		aux.add(p);
                                                		hm.put(currentWord,aux);
                                        		}
                                        		cont++;
                                		}
                        		}	
                		}
        		}
		}
	}

	private static String[] getTagValue(String sTag, Element eElement) 
	{
		//we supopse there are not more than two different abstracts
		
		String[] returnStrings = new String[3];
		NodeList nList = eElement.getElementsByTagName(sTag);
                
		if (nList.getLength()>0)
                {
                      	//for every element containing the tag name we get their childs
			for( int i = 0; i < nList.getLength(); i++)
                        {
                        	NodeList auxList =  nList.item(i).getChildNodes();
                                //we get the value of every child and put it into the returning array. We suppose there is only one child
				if (auxList.getLength()>0)
					for (int j = 0; j < auxList.getLength(); j++)
                               			returnStrings[i] = auxList.item(j).getNodeValue();
                        }
                }
		return returnStrings;
	}

	public Map<String,List<PointerPair>> get ()
	{
		return hm;
	}


	public static void main(String[] args)
	{
		try
		{
			XmlParser parser = new XmlParser("../test/test_08n0147.xml");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}	
	}	
}
