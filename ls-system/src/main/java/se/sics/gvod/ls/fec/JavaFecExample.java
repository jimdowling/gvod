package se.sics.gvod.ls.fec;
/**
    Java FEC Example
    ---
    Latest version and comments can be found at:
        http://jamesclarke.info/notes/javafec

    This is an example of how to use the Java FEC library by Onion
    Networks by James Clarke (james@jamesclarke.info).  Please note I
    am in no way affiliated with Onion Networks, nor had any part in
    writing the Java FEC library.
    
    "Effective erasure codes for reliable computer communication
    protocols" by Luigi Rizzo provides details on how forward error
    correction works.  And I believe that the Java FEC library is
    based on L. Rizzo's code.
    
    If you find any problems with the example please contact me,
    preferably with a solution :)

    You may study, use, modify, and distribute this example for any
    purpose.  This example is provided WITHOUT WARRANTY either
    expressed or implied.
    
    URLs: "Effective erasure codes for reliable computer communication
        protocols" http://citeseer.nj.nec.com/rizzo97effective.html
            
        Java FEC Library: http://onionnetworks.com/developers/index.php
        
        James Clarke (james@jamesclarke.info): http://jamesclarke.info
        
**/

import com.onionnetworks.fec.FECCode;
import com.onionnetworks.fec.FECCodeFactory;
import com.onionnetworks.util.Buffer;

import java.util.Random;
import java.util.Arrays;

public class JavaFecExample {
    
    public static void main(String args[]) {
        //k = number of source packets to encode
        //n = number of packets to encode to
        int k = 16;
        int n = 32;
        int packetsize = 1024;
        
        
        Random rand = new Random();
        
        byte[] source = new byte[k*packetsize]; //this is our source file
        
        //NOTE: The source needs to split into k*packetsize sections
        //So if your file is not of the right size you need to split
        //it into groups.  The final group may be less than
        //k*packetsize, in which case you must pad it until you read
        //k*packetsize.  And send the length of the file so that you
        //know where to cut it once decoded.
        
        //this is just so we have something to encode
        rand.nextBytes(source);     
        
        //this will hold the encoded file
        byte[] repair = new byte[n*packetsize]; 
        
        //These buffers allow us to put our data in them they
        //reference a packet length of the file (or at least will once
        //we fill them)
        Buffer[] sourceBuffer = new Buffer[k];
        Buffer[] repairBuffer = new Buffer[n];
        
        for (int i = 0; i < sourceBuffer.length; i++)
            sourceBuffer[i] = new Buffer(source, i*packetsize, packetsize);
            
        for (int i = 0; i < repairBuffer.length; i++)
            repairBuffer[i] = new Buffer(repair, i*packetsize, packetsize);
        
        //When sending the data you must identify what it's index was.
        //Will be shown and explained later
        int[] repairIndex = new int[n];
    
        for (int i = 0; i < repairIndex.length; i++)
            repairIndex[i] = i;
        
        //create our fec code
        FECCode fec = FECCodeFactory.getDefault().createFECCode(k,n);
        
        //encode the data
        fec.encode(sourceBuffer, repairBuffer, repairIndex);
        //encoded data is now contained in the repairBuffer/repair byte array
        
        //From here you can send each 'packet' of the encoded data, along with
        //what repairIndex it has.  Also include the group number if you had to
        //split the file
            
        //We only need to store k, packets received
        //Don't forget we need the index value for each packet too
        Buffer[] receiverBuffer = new Buffer[k];
        int[] receiverIndex = new int[k];

        //this will store the received packets to be decoded
        byte[] received = new byte[k*packetsize];
        
        //We will simulate dropping every even packet
        int j = 0; 
        for (int i = 0; i < n; i++) {
            if (i % 2 == 2)
                continue;
            byte[] packet = repairBuffer[i].getBytes();
            System.arraycopy(packet, 0, received, j*packetsize, packet.length);
            receiverIndex[j] = i;
            j++;
        }
        
        //create our Buffers for the encoded data
        for (int i = 0; i < k; i++)
            receiverBuffer[i] = new Buffer(received, i*packetsize, packetsize);
        
        //finally we can decode
        fec.decode(receiverBuffer, receiverIndex);
        
        //check for equality
        if (Arrays.equals(source, received))
            System.out.println("Source and Received Files are equal!");
        else
            System.out.println("Source and Received Files are different!");
    }//end main
}//end class
