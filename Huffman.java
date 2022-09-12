import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.io.FileWriter;

public class Huffman<K,V> implements PrefixCode {
    PriorityQueue<Node> p= new PriorityQueue<Node>();

    HashMap<Integer,String> map = new HashMap<Integer,String>();


    Node root = new Node();

    int compressedWeight=0;


    public void generateCode(InputStream in){
        HashMap<Integer,Integer> hash=new HashMap<Integer,Integer>();

        //try, else throw exception as required by the read() method
        try{
        
        //creates a variable that reads the first char
        int input = in.read();

        //increment number of appearances of each char that is read, breaks if
        //it reads -1 (end of text)
        while(input!=(-1)){


                    //if no value has been hashed to that char yet, give it value 1
                    if(hash.get(input)==null){
                        hash.put(input,1);
                    }

                    //if a value already exists at that key, increases said value by 1
                    else if(hash.get(input)!=null){
                        hash.put(input,hash.get(input)+1);
                    }
                    //assigns a new char value to input
                    input = in.read();
                }
            
    //catches possible exceptions to the input reading
    } catch (IOException e){
        System.out.println("IOException encountered");
    }


    //loops through all keys and defines a weight variable with the key appearance frequency
    for(Integer val: hash.keySet()){
        int weight=hash.get(val);

        //initializes a node with the chosen value and weight, then adds it to the priority queue
        Node nd = new Node();
            nd.value=val;
            nd.weight=weight;
            p.add(nd);
    }

    //while the priority queue is not empty, creates a tree according to the following process:
    //removes two highest priority element of p and creates a parent node with their combined weights and a value of 0
    //this node is added back into p
    //repeats this process till one node remains in p
    while(p.size()>1){

         Node nd0= new Node();
         nd0=p.peek();
         p.remove(p.peek());

         Node nd1= new Node();
         nd1=p.peek();
         p.remove(p.peek());

         Node nd2=new Node();
         nd2.rightChild=nd0;
         nd2.leftChild=nd1;
         nd1.parent=nd2;
         nd0.parent=nd2;

         nd2.weight=(int) nd1.weight+ (int) nd0.weight;
         nd2.value=0;
         p.add(nd2);

    }

    //completes the tree by adding the root, then calls the traversal method on it, which builds the ASCII value/binary code hash map
    root=p.peek();
    p.remove(p.peek());
    traversal(root,"");
}


//recursive method which traverses the Huffman tree and adds ASCII value/binary encoding combinations to the hash map
public void traversal(Node<Integer> nd, String binaryCode){

    //if a leaf is reached, add the node ASCII number (key) and binary Code (value) to the hash map
    if(nd.leftChild==null && nd.rightChild==null){
        int ASCIIval = (int) nd.value;
        map.put(ASCIIval,binaryCode);
        return;
    }

    //add a 1 or 0 to the output binary code string depending on whether it is a right or left child of the previous node
    traversal(nd.leftChild, binaryCode+"0");
    traversal(nd.rightChild, binaryCode+"1");

}


    //checks the hash map for the input char's ASCII value
    //if found, returns the associated value, otherwise it returns an empty string ""
    public String getCodeword(char ch){

        if(map.get((int) ch)!=null){
            return map.get((int) ch);
        }
        else{
            return "";
        }
    }


    //iterates through the Huffman tree depending on the input binary codeword
    public int getChar(String codeword){

        //creates a node nd at the root
        Node nd = new Node();
        nd=root;

        //iterates through the string and moves through the tree depending on each char encountered
        for(int i=0; i<codeword.length();i++){
            char ch=codeword.charAt(i);
            
            //if a 1 is encountered, moves nd right
            if(ch=='1'){
                nd=nd.rightChild;
            }

            //if a 0 is encountered, moves nd left and returns its new value
            else if(ch=='0'){
                nd=nd.leftChild;

                //if nd has the same value as the ch ASCII, returns said value
                if((int)nd.value==(int)ch){
                    return (int) nd.value;
            }
        }
    }
            //returns the value of nd if it reaches the rightmost child of the tree
            return (int) nd.value;
}


    //iterates through an input string and records its encoded equivalent
    public String encode(String str){

        String encoded="";
        String encodedchar;
        int length= str.length();
        char[] char_array= new char[length];

        //places all characters of the input str into an array
        str.getChars(0,str.length(),char_array,0);

        //iterates through every element of the array and gets its corresponding codeword, then appends it to the final string
        for ( int i=0; i<str.length(); i++){
            encodedchar=getCodeword(char_array[i]);
            encoded=encoded+encodedchar;
                }

        return encoded;
    }


    //converts an input codeword string into an output stream of characters
    public String decode(String str){

        String decoded="";

        String decoding="";

        //iterates through the input encoded str and adds each character (1 or 0) to the decoding variable
        for(int i=0;i<str.length();i++){
            decoding=decoding+str.charAt(i);

            //every time a character is added, iterates through the hash map keys to check whether the total string "decoding" 
            //corresponds to a hash map value
            //if so, adds the character corresponding to that value to the "decoded" string, resets the "decoding" string and breaks
            for(Integer val: map.keySet()){

                if(decoding.equals(map.get(val))){
                    char cr = (char) (int) val;
                    decoded=decoded+cr;
                    decoding="";
                    break;
                }
            }
        }
        return decoded;
    }


    //returns the weight of the root, which also corresponds to the size of the initial encoding in bytes
    public int originalSize(){

        //returns the weight of the root, which is the sum frequency of all characters
        return (int) root.weight;
    }


//recursive method that iterates through our huffman tree and calculates the total number of bits used in compression
public void compressedSizeCalc(Node<Integer> nd){

    //adds the product of each node's weight and ASCII equivalent character's codeword length to a sum variable compressedWeight
    if(nd.leftChild==null && nd.rightChild==null){
        char character = (char) (int) nd.value;
        String encoded = getCodeword(character);
        compressedWeight=compressedWeight+nd.weight*encoded.length();
        return;
    }

    //iterates recursively through the whole tree
    compressedSizeCalc(nd.leftChild);
    compressedSizeCalc(nd.rightChild);
}


    //calls the recursive method compressedSizeCalc and returns the compressed weight (in bytes)
    public int compressedSize(){

         compressedSizeCalc(root);
        return compressedWeight/8;
    }


    //defines a node class implementing the comparable interface
    //nodes are compared based on weights
    private class Node<Integer extends Comparable<Integer>> implements Comparable<Node<Integer>>{
        Integer value;
        Integer weight;
        Node<Integer> parent;
        Node<Integer> leftChild;
        Node<Integer> rightChild;

        @Override
        public int compareTo(Node<Integer> nd){
            return weight.compareTo(nd.weight);
        }
    }
}

