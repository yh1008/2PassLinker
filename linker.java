/**
 * @author Ye Hua Lab1 2-pass linker simualtor Due 02/09/2015 
 */
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Arrays;

public class linker{

	//store symbol and the module number it is in
	static HashMap<String, Integer> symbolDef = new HashMap<String, Integer>();
	//store symbol and its absolute address
	static HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();
	//store module and its length
	static HashMap<Integer,Integer> moduleLen = new HashMap<Integer,Integer>();
	//store module number and its base location
	static HashMap<Integer,Integer>  moduleBase = new HashMap<Integer,Integer> ();
	//store which symbol(s) is used in this module (store symbol, start_loc)
	static HashMap<String, Integer> symbolInUse = new HashMap<String, Integer>();
	//store error message
	static ArrayList<String> errorMessage = new ArrayList<String>(); 
    /****************************FirstPass******************/
    public static void firstPass(String fileName) throws FileNotFoundException{
    	//relocationConstant = length of all the previous module
    	int relocationConstant = 0;
    	int moduleNumber = 1;
    	//read in the input file 
    	FileReader fileReader = new FileReader(fileName);
    	BufferedReader file = new BufferedReader(fileReader);
    	Scanner input = new Scanner(file);

    	//process the definition list
    	while (input.hasNext()){
    		moduleBase.put(moduleNumber, relocationConstant);
    		//find the symbol location
    		int number_of_defs= input.nextInt();
    		for (int i = 0; i < number_of_defs; i++){

    			String symbol = input.next();
    			int relativeAddress = input.nextInt();
    			//error handling goes here...
    			if(symbolDef.get(symbol) != null){
    				errorMessage.add("Error symbol " + symbol + " is multiply defined, and the first definition is used");
    			}
    			else {
    			symbolDef.put(symbol, moduleNumber);
    			symbolTable.put(symbol, relocationConstant+relativeAddress);
    			}
    		}
    		//skip the use list
    		int number_of_use = input.nextInt();
    		for (int i = 0; i < number_of_use; i++){
    			input.next();
    			input.nextInt();
    		}
    		//get the module length; and skip the rest of program text
    		int number_of_instructions = input.nextInt();
    		moduleLen.put(moduleNumber, number_of_instructions);
    		//update relocation constant for next module by adding the (previous) module length(number_of_instructions)
    		relocationConstant += number_of_instructions;
    		//skip the rest of program text content
    		for (int i = 0; i < number_of_instructions; i++){
    			input.next();
    			input.nextInt();
    		}
    		moduleNumber++;
    	}
    	//handle if a symbol exceeds the size of module, last word in module is used
    	//handle if definition excees module size, last word is used
    	Set setOfKeys_1 = symbolDef.keySet();
		Iterator iterator_1 = setOfKeys_1.iterator();
		while(iterator_1.hasNext()){
			String key = (String) iterator_1.next();
			Integer value = (Integer) symbolDef.get(key);
			int baseAddress = (int) moduleBase.get(value);
			int absoluteAddress = (int) symbolTable.get(key);
			int moduleSize = (int) moduleLen.get(value);
			if (absoluteAddress - baseAddress >= moduleSize){
				errorMessage.add("Error symbol " + key + " is defined at an address that exceeds the size of the module, and treat the relative address given as 0");
				symbolTable.put(key, baseAddress);
			}
		}

    	//symbol table creation 
        int[] valueList = new int[symbolTable.size()];
        Set setOfKeys_3 = symbolTable.keySet();
        Iterator iterator_3 = setOfKeys_3.iterator();
        int j = 0;
        while (iterator_3.hasNext()){
            String key = (String) iterator_3.next();
            Integer value = (Integer) symbolTable.get(key);
            valueList[j] = value;
            j++;
        }
        Arrays.sort(valueList);
    	System.out.println("Symbol Table\n");
        for (int i = 0; i < symbolTable.size(); i++){
    		int value = valueList[i];
            Set setOfKeys_2 = symbolTable.keySet();
            Iterator iterator_2 = setOfKeys_2.iterator();
            while (iterator_2.hasNext()){
                String key = (String) iterator_2.next();
                if ((int) symbolTable.get(key) == value){
                    System.out.format("%s = %d \n", key, value );
                    break;

                }
            }
    		

    	}   	

    }
   
 	/****************************SecondPass***********************/
 	public static void secondPass(String fileName) throws FileNotFoundException {
 		//read in the input file 
    	FileReader fileReader = new FileReader(fileName);
    	BufferedReader file = new BufferedReader(fileReader);
    	Scanner input = new Scanner(file);
    	int index = 0; //keeps track of how many instructions within this module has we processed
    	int moduleNumber = 1;
    	int firstDigit = 0;
    	int correct_address = 0;   	
    	String symbol = new String();
    	System.out.println("\nMemory Map");

    	while(input.hasNext()){
    		HashMap<String, Integer> used_symbol_in_module = new HashMap<String, Integer>();
    		HashMap<Integer, Integer> ins_location = new HashMap<Integer, Integer>(); //helps to access the instruction starts at any relative loc in the module
    		HashMap<Integer, String> type_location = new HashMap<Integer, String>(); //look up which type occurs at a particular location
    		int relocationConstant = (int) moduleBase.get(moduleNumber);
    		//skip definition list
    		int number_of_defs = input.nextInt();
    		for (int i = 0; i < number_of_defs; i++){
    			input.next();
    			input.nextInt();
    		}
    		//process use list
    		int number_of_use = input.nextInt();
    		for (int i = 0; i < number_of_use; i++) {
    			symbol = input.next();
    			int start_loc = input.nextInt();
    			symbolInUse.put(symbol, start_loc); //symbol used across module
    			used_symbol_in_module.put(symbol, start_loc); //only store symbols used within the same module
    		}
    		//process program text
    		int number_of_instructions = input.nextInt();
    
    		for (int i = 0; i < number_of_instructions; i++) {
    			//a pair is (type, word)
    			String type = input.next();
    			int word = input.nextInt();    			
    			//store word in the ins_location hashmap
    			ins_location.put(i, word);
    			type_location.put(i, type);
    	
    		}
    		Set setOfKeys_2 = used_symbol_in_module.keySet();
			Iterator iterator_2 = setOfKeys_2.iterator();
			int counter = 0; //keeps track of how many times a use exceeds module size
			if (setOfKeys_2.isEmpty()){
				counter = number_of_instructions;
			}
			while(iterator_2.hasNext()){
				String key = (String) iterator_2.next();
				int moduleSize = (int) moduleLen.get(moduleNumber);
				int address_in_use = (int) symbolInUse.get(key);
				if (address_in_use > moduleSize){
					errorMessage.add("Error: symbol " + key + " in use list has an address that exceeds the size of the module, the address has been treated as 777");
					ins_location.put(address_in_use, 1777);
					type_location.put(address_in_use,"E");
					counter = address_in_use + 1;

				}
				else {
					counter = number_of_instructions;
				}
			}   		
    		int[] corrected_address_list = new int[counter];   		
    		//warning: key was defined but the value was never used. goes here
    		//calculation 
    		//in case there is more than 1 symbol in a use list
    		Set setOfKeys = used_symbol_in_module.keySet();
    		Iterator iterator = setOfKeys.iterator();
    		if (setOfKeys.isEmpty()){
    			for (int i = 0; i < number_of_instructions; i++) {
    				if (corrected_address_list[i] == 0){
    					if (type_location.get(i).equals("A") || type_location.get(i).equals("I")) {
    						corrected_address_list[i] = (int)ins_location.get(i);
    					}
    					if (type_location.get(i).equals("R")){
    						corrected_address_list[i] = relocationConstant + (int)ins_location.get(i);

    					} 
    					if (type_location.get(i).equals("E")){
    						corrected_address_list[i] = (int)ins_location.get(i);
    						errorMessage.add("Error: at instruction" + i + "within module " + (moduleNumber-1) +" E type address not on use chain; treated as I type.");
    					}
    				}
    			}
    			//display nicely!   			
    			for (int i = 0; i < number_of_instructions; i++) {
    				System.out.format("%d:	%d\n", index, corrected_address_list[i]);
    				index++;
    			}
    		}
    		while(iterator.hasNext()){
    			String symbol_in_use = (String) iterator.next();
    			Integer start_loc = (Integer)symbolInUse.get(symbol_in_use);
    			while (start_loc != 777){
                                    //error handle if symbol is used buy not defined.               
                    if(symbolDef.get(symbol_in_use) == null){
                        errorMessage.add("Error symbol " + symbol + " is not defined, and its value is given zero at instruction " + (start_loc+1) + " within module " + moduleNumber);
                        //start_loc = 0;
                        symbolTable.put(symbol, 0);
                    }
    				int temp_address = (int)ins_location.get(start_loc);
    				if (type_location.get(start_loc).equals("E")){
    					firstDigit = temp_address/1000;
    					Integer absoluteAddress = (Integer)symbolTable.get(symbol_in_use);
    					correct_address = firstDigit*1000+absoluteAddress;
    					corrected_address_list[start_loc] = correct_address;
    					start_loc = temp_address%1000;
    					//check if the next address directs you to an address that exceeds module size. 
    					if (start_loc != 777 && ((int)ins_location.get(start_loc))%1000 != 777 && ins_location.get(((int)ins_location.get(start_loc))%1000) == null){
    						errorMessage.add("Error: symbol " + symbol_in_use + " in use list has an address that exceeds the size of the module, the address has been treated as 777");
    						corrected_address_list[start_loc] = ((int)ins_location.get(start_loc)/1000)*1000+(int)symbolTable.get(symbol_in_use);
    						break;
    					}
    				}
    				else {
    					String misplaced_symbol = (String)type_location.get(start_loc);
    					errorMessage.add("Error: " + misplaced_symbol + " type address on use chain; treated as E type.");
    					firstDigit = temp_address/1000;
    					Integer absoluteAddress = (Integer)symbolTable.get(symbol_in_use);
    					corrected_address_list[start_loc] = firstDigit*1000+absoluteAddress;
    					start_loc = temp_address%1000;  				
    				}
    			}
    		}
    		if (!setOfKeys.isEmpty()){
    			for (int i = 0; i < number_of_instructions; i++) {
    				if (corrected_address_list[i] == 0){
    					if (type_location.get(i).equals("A") || type_location.get(i).equals("I")) {
    						corrected_address_list[i] = (int)ins_location.get(i);
    					}
    					if (type_location.get(i).equals("R")){
    						corrected_address_list[i] = relocationConstant + (int)ins_location.get(i);
    					} 
    					if (type_location.get(i).equals("E")){
    						corrected_address_list[i] = (int)ins_location.get(i);
    						errorMessage.add("Error: at instruction " + i + " within module " + (moduleNumber-1) + ", E type address not on use chain; treated as I type.");
    					}

    				}
    			}    		    			
    			//display nicely!  			
    			for (int i = 0; i < number_of_instructions; i++) {
    				System.out.format("%d:	%d\n", index, corrected_address_list[i]);
    				index++;
    			}
    		}    		
    		//the end of the process for the current module; move to the next one.
    		moduleNumber++;
    	}
    	//error handling: symbol defined but not used 
    	Set symbolKeys_1 = symbolDef.keySet();
		Iterator iterator_1 = symbolKeys_1.iterator();
		while (iterator_1.hasNext()) {
			String key = (String) iterator_1.next();
			Integer value = (Integer) symbolDef.get(key);
			if (!symbolInUse.containsKey(key)) {				
				System.out.println("Warning: " + key + " was defined in module " + (value-1) +" but never used.");
			}
		}
	}   
    /****************************SecondPass***********************/
 	public static void displayErrorMessage(ArrayList<String> errorMessage){
 		for (int i = 0; i < errorMessage.size(); i++){
 			System.out.println(errorMessage.get(i));
 		}
 	}
    /****************************Main***********************/
	public static void main(String[] args) throws IOException {
		String fileName = args[0];
		firstPass(fileName);
		secondPass(fileName);
		displayErrorMessage(errorMessage);

	}
}
