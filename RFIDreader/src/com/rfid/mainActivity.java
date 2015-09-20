package com.rfid;

import java.io.File;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;

import com.thingmagic.*;

public class mainActivity {
	//connecting uri
	private static String uriString = "tmr:///dev/cu.usbserial-A402JMEX";
	private static Calculater cal = new Calculater();
	
	static void usage()
	  {
	    System.out.printf("Usage: Please provide valid arguments, such as:\n"
	                + "  (URI: 'tmr:///COM1 --ant 1,2' or 'tmr://astra-2100d3/ --ant 1,2' "
	                + "or 'tmr:///dev/ttyS0 --ant 1,2')\n\n");
	    System.exit(1);
	  }

	   public static void setTrace(Reader r, String args[])
	  {    
	    if (args[0].toLowerCase().equals("on"))
	    {
	      r.addTransportListener(r.simpleTransportListener);
	    }    
	  }
	   
	public static void main(String argv[]) {
		// TODO Auto-generated method stub
		// Program setup
	    Reader r = null;
	    int nextarg = 0;
	    boolean trace = false;
	    int[] antennaList = null;
	    
	    // Create Reader object, connecting to physical device
	    try
	    { 
	     
	        TagReadData[] tagReads;
	        String readerURI = uriString;
	        nextarg++;

	        for (; nextarg < argv.length; nextarg++)
	        {
	            String arg = argv[nextarg];
	            if (arg.equalsIgnoreCase("--ant"))
	            {
	                if (antennaList != null)
	                {
	                    System.out.println("Duplicate argument: --ant specified more than once");
	                    usage();
	                }
	                antennaList = parseAntennaList(argv, nextarg);
	                nextarg++;
	            }
	            else
	            {
	                System.out.println("Argument " + argv[nextarg] + " is not recognised");
	                usage();
	            }
	        }

	        r = Reader.create(readerURI);
	        if (trace)
	        {
	            setTrace(r, new String[] {"on"});
	        }
	        r.connect();
	        if (Reader.Region.UNSPEC == (Reader.Region) r.paramGet("/reader/region/id"))
	        {
	            Reader.Region[] supportedRegions = (Reader.Region[]) r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
	            if (supportedRegions.length < 1)
	            {
	                throw new Exception("Reader doesn't support any regions");
	            }
	            else
	            {
	                r.paramSet("/reader/region/id", supportedRegions[0]);
	            }
	        }

	        String model = r.paramGet("/reader/version/model").toString();
	        if ((model.equalsIgnoreCase("M6e Micro") || model.equalsIgnoreCase("M6e Nano")) && antennaList == null)
	        {
	            System.out.println("Module doesn't has antenna detection support, please provide antenna list");
	            r.destroy();
	            usage();
	        }
	        
	        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
	        r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
	        
	        // for collecting data
	        String pathname = "/Users/Ben/Desktop/dataoutput.txt";
			DataWriter datawriter = new DataWriter();
			String datastring = new String();
			String yesno = new String();
			// testing data
			Instances testinstance = WekaDemo.createInstances();
			
			for(int i=0; i<20; i++){
	              
			// Read tags
	        tagReads = r.read(500);
	        
			// Print tag reads
	        for (TagReadData tr : tagReads)
	        {
	        	double distance = cal.calDistanceByRSSI(tr.getRssi());
	            System.out.println(tr.toString()+" RSSI: "+tr.getRssi()+" Dis :"+distance+" Frequency: "+tr.getFrequency()+" Phase: "+tr.getPhase());
	            
	            //for collect data
//	            if(tr.epcString().endsWith("1")|tr.epcString().endsWith("2")|tr.epcString().endsWith("3")){
//	            	yesno = ",yes \n";
//	            }else{
//	            	yesno = ",no \n";
//	            }
//	            datastring = tr.getRssi()+","+tr.getFrequency()+","+tr.getPhase()+yesno;
//				datawriter.DataOutput(pathname, datastring);
	            
	  
	         
	            Instance iExample = new Instance(4);
	            iExample.setValue(0, tr.getRssi());
	            iExample.setValue(1, tr.getFrequency());
	            iExample.setValue(2, tr.getPhase());
	            testinstance.add(iExample);
	        }
			}
	        
	        WekaDemo.calculateResult(testinstance);

	        // Shut down reader
	        r.destroy();
	    } 
	    catch (ReaderException re)
	    {
	      System.out.println("Reader Exception : " + re.getMessage());
	    }
	    catch (Exception re)
	    {
	        System.out.println("Exception : " + re.getMessage());
	    }

}
	static  int[] parseAntennaList(String[] args,int argPosition)
    {
        int[] antennaList = null;
        try
        {
            String argument = args[argPosition + 1];
            String[] antennas = argument.split(",");
            int i = 0;
            antennaList = new int[antennas.length];
            for (String ant : antennas)
            {
                antennaList[i] = Integer.parseInt(ant);
                i++;
            }
        }
        catch (IndexOutOfBoundsException ex)
        {
            System.out.println("Missing argument after " + args[argPosition]);
            usage();
        }
        catch (Exception ex)
        {
            System.out.println("Invalid argument at position " + (argPosition + 1) + ". " + ex.getMessage());
            usage();
        }
        return antennaList;
    }	
	
	public static void memReadWords(Reader r, String args[])
	  {
//	    TagFilter target;
//	    int bank, address, count;
//	    short[] values;
//
//	    bank = Integer.decode(args[0]);
//	    address = Integer.decode(args[1]);
//	    count = Integer.decode(args[2]);
//
//	    if (args.length > 3)
//	      target = (TagFilter)parseValue(args[3]);
//	    else
//	      target = select;
//
//	    try
//	    {
//	      if(r instanceof SerialReader || r instanceof RqlReader)
//	      {
//	      values = r.readTagMemWords(target, bank, address, count);
//	      }
//	      else
//	      {
//	          Gen2.ReadData rData = new Gen2.ReadData(Gen2.Bank.getBank(bank), address, (byte)count);
//	          values = (short[])r.executeTagOp(rData, target);
//	      }
//	      System.out.printf("words:");
//	      for (int i = 0; i < values.length; i++)
//	      {
//	        System.out.printf("%04x", values[i]);
//	      }
//	      System.out.printf("\n");
//	    }
//	    catch (ReaderException re)
//	    {
//	      System.out.printf("Error reading memory of tag: %s\n",
//	                        re.getMessage());
//	    }
	  }
}
