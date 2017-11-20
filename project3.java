import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class project3 {
	
	public static void main(String[] args) throws IOException{
		UserInterface ui = new UserInterface(new Engine());
		ui.mainLoop();
		
	}

}

class UserInterface{
	/**
	 * This field represents the engine of the interface. This is the main engine for the program.
	 */
	private Engine engine;

	private String helpMessage = 
			"Q: Query the city information by entering the city code.\n"
			+ "D: Find the minimum distance between two cities.\n"
			+ "I: Insert a road by entering two city codes and distance.\n"
			+ "R: Remove an existing road by entering two city codes.\n"
			+ "H: Display this message.\n"
			+ "E: Exit";
	
	private String exitMessage = "Thank you for using the program!";
	
	public UserInterface(Engine engine){
		this.engine = engine;
	}
	
	public void mainLoop(){
		Scanner keyboard = new Scanner(System.in);
		String line = "";
		char command = 'z';
		boolean exit = false;
		boolean valid = true;
		
		while(!exit){
			
			do{
				valid = true;
				System.out.print("Enter a command: ");
				line = keyboard.nextLine();
				command = line.toUpperCase().charAt(0);
				
				
				switch(command){
				case 'Q':
					// find the city information
					queryCity(keyboard);
					break;
				case 'D':
					// find the minimum distance between two cities
					break;
				case 'I':
					// insert a road between two cities
					addRoad(keyboard);
					break;
				case 'R':
					// Remove an existing road between two cities
					removeRoad(keyboard);
					break;
				case 'H':
					// display help message
					System.out.println(helpMessage);
					break;
				case 'E':
					// exit the program
					System.out.println(exitMessage);
					exit = true;
					break;
					default:
						// invalid command
						System.out.println("Invalid command! Enter \"H\" if you need help.");
						valid = false;
						break;
				}
			}while(!valid);	
		}
		keyboard.close();
	}
	
	/**
	 * This method asks for a city code and prints information about the city if found. Otherwise
	 * it says that no city was found.
	 * @param keyboard - A scanner.
	 */
	public void queryCity(Scanner keyboard){
		System.out.print("Enter a city code: ");
		String line = keyboard.nextLine();
		String[] elements = line.split("\\s+");
		String cityCode = elements[0];
		System.out.print(engine.getCityInformation(cityCode) + "\n");
	}
	
	/**
	 * This method adds a road to the roads array. The method will ask for all the required
	 * input and also check for valid input. 
	 * 
	 * @param keyboard
	 */
	public void addRoad(Scanner keyboard){
		boolean valid = true;
		System.out.print("Enter a city codes and a distance: ");
		String line = keyboard.nextLine();
		line = line.trim();
		String[] elements = line.split("\\s+");
		String fromCityCode = elements[0];
		String toCityCode = elements[1];
		int distance = -1;
		
		try{
			distance = Integer.parseInt(elements[2]);
		}catch(NumberFormatException e){
			System.out.println("That distance was the wrong format!");
			valid = false;
			do{
				try{
					System.out.print("Try entering it again as an integer: ");
					line = keyboard.nextLine();
					line = line.trim();
					String[] secondElements = line.split("\\s+");
					distance = Integer.parseInt(secondElements[0]);
					valid = true;
				}catch(NumberFormatException second){
					System.out.println("Still the incorrect format. Try again.");
				}
			}while(!valid);
		}
		
		
		
		City fromCity = engine.getCityObject(fromCityCode);
		City toCity = engine.getCityObject(toCityCode);
		if(fromCity != null){
			if(toCity != null){
				// cities both exist
				boolean inserted = engine.insertRoad(fromCityCode, toCityCode, distance);
				if(inserted){
					// inserted road
					System.out.println("Inserted road from " + fromCity.getFullCityName() + " to " + toCity.getFullCityName());
				}else{
					// road already exists
					System.out.println("That road already exist!");
				}
			}else{
				// to city does not exist
				System.out.println("City with code '" + toCityCode + "' does not exist!");
			}
		}else{
			// formCity does not exist
			System.out.println("City with code '" + fromCityCode + "' does not exist!");
		}
	}
	
	/**
	 * This method removes a road from the roads array. The method will validate the correct input and notify the
	 * user of any issues. 
	 * 
	 * @param keyboard
	 */
	public void removeRoad(Scanner keyboard){
		System.out.print("Enter a city codes: ");
		String line = keyboard.nextLine();
		line = line.trim();
		String[] elements = line.split("\\s+");
		String fromCityCode = elements[0];
		String toCityCode = elements[1];
		
		City fromCity = engine.getCityObject(fromCityCode);
		City toCity = engine.getCityObject(toCityCode);
		if(fromCity != null){
			if(toCity != null){
				// cities both exist
				boolean deleted = engine.deleteRoad(fromCityCode, toCityCode);
				if(deleted){
					// deleted road
					System.out.println("Deleted road from " + fromCity.getFullCityName() + " to " + toCity.getFullCityName());
				}else{
					// road doesn't exist
					System.out.println("That road doesn't exist!");
				}
			}else{
				// to city does not exist
				System.out.println("City with code '" + toCityCode + "' does not exist!");
			}
		}else{
			// formCity does not exist
			System.out.println("City with code '" + fromCityCode + "' does not exist!");
		}
		
	}
}

class Engine{
	/**
	 * This field represents all the cities loaded from the data file.
	 */
	private ArrayList<City> cities;
	
	/**
	 * This field represents all the roads loaded from the data file.
	 */
	private ArrayList<Road> roads;
	
	/**
	 * This is the default constructor.  It uses the default filenames for the data files.
	 * @throws IOException
	 */
	public Engine() throws IOException{
		this("city.dat", "road.dat");
	}
	
	public Engine(String cityFileName, String roadFileName) throws IOException{
		InputStream cityFile = this.getClass().getResourceAsStream(cityFileName);
		InputStream roadFile = this.getClass().getResourceAsStream(roadFileName);
		
		// load the cities from the city file
		loadCities(cityFile);
		
		// load the roads from the road file
		loadRoads(roadFile);
	}
	
	/**
	 * This method populates the {@link #cities} array with cities found in the specified data file.
	 * 
	 * @param file - An InputStream representation of the data file containing the city data.
	 * @throws IOException
	 */
	private void loadCities(InputStream file) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		ArrayList<String> lines = new ArrayList<String>();
		String line = "";
		cities = new ArrayList<City>();
		
		// read each non-blank line from the file into an ArrayList.
		while((line = reader.readLine()) != null){
			if(line.length() > 0){
				lines.add(line);
			}
		}
		
		// close file reader
		reader.close();
		
		// populate the cities array by creating City objects
		for(int i = 0; i < lines.size(); i++){
			// The name may have an undetermined number of spaces,
			// so there needs to be a check for this.
			boolean doneReadingFullName = false;
			int currentIndex = 0;
			line = lines.get(i).trim();
			String[] elements = line.split("\\s+");
			
			// extract the first data elements from the line.
			int cityNumber = Integer.parseInt(elements[currentIndex]);
			String cityCode = elements[++currentIndex];
			String fullCityName = elements[++currentIndex];
			
			// loop until an integer is reached to be sure the full 
			// city name is extracted.
			int population = -1;
			do{
				
				try{
					population = Integer.parseInt(elements[++currentIndex]);
					doneReadingFullName = true;
				}catch(NumberFormatException e){
					// there is another part to the name
					fullCityName += " " + elements[currentIndex];
				}
			}while(!doneReadingFullName);
			
			int elevation = Integer.parseInt(elements[++currentIndex]);
			
			// create a city object inside the cities array.
			cities.add(new City(cityNumber, cityCode, fullCityName, population, elevation));
		}
		
	}
	
	/**
	 * This method populates the {@link #roads} array with the roads found in the specified data file.
	 * 
	 * @param file - An InputStream representation of the data file containing the road data.
	 * @throws IOException
	 */
	private void loadRoads(InputStream file) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		ArrayList<String> lines = new ArrayList<String>();
		String line = "";
		roads = new ArrayList<Road>();
		
		// read each non-blank line from the file into an ArrayList.
		while((line = reader.readLine()) != null){
			if(line.length() > 0){
				lines.add(line);
			}
		}
		
		// close file reader
		reader.close();
		
		// populate the roads array by creating Road objects
		for(int i = 0; i < lines.size(); i++){
			int currentIndex = 0;
			
			line = lines.get(i).trim();
			String[] elements = line.split("\\s+");
			
			// extract the data from the line.
			int fromCity = Integer.parseInt(elements[currentIndex]);
			int toCity = Integer.parseInt(elements[++currentIndex]);
			int distance = Integer.parseInt(elements[++currentIndex]);
			
			// create the road object and place it inside the roads array.
			roads.add(new Road(fromCity, toCity, distance));
		}
	}
	
	/**
	 * @param cities - An array of City objects.
	 * @param cityCode - A unique two-character city code.
	 * @return An integer city number of a City in the specified array, given the specified city code. Returns -1 if no city found from the
	 * specified code.
	 */
	public int getCityNumberFromCityCode(String cityCode){
		int cityNumber = -1;
		for(City city: cities){
			if(city.getCityCode().equals(cityCode)){
				cityNumber = city.getCityNumber();
			}
		}
		return cityNumber;
	}
	
	/**
	 * This method takes two city numbers as parameters and determines if there is a road between them.
	 * 
	 * @param fromCity - The city number of the origin city.
	 * @param toCity - The city number of the destination city.
	 * @return True if the road exists.
	 */
	public boolean roadExists(int fromCity, int toCity){
		boolean exists = false;
		for(Road road: roads){
			if((road.getFromCity() == fromCity) && (road.getToCity() == toCity)){
				exists = true;
			}
		}
		return exists;
	}
	
	/**
	 * This method inserts a road into the roads array.
	 * 
	 * @param fromCity - The city number of the origin city.
	 * @param toCity - The city number of the destination city.
	 * @param distance - The distance between the cities.
	 * @return True if the road was inserted, false if the road already exists.
	 */
	public boolean insertRoad(int fromCity, int toCity, int distance){
		boolean inserted = false;
		if(!roadExists(fromCity, toCity)){
			roads.add(new Road(fromCity, toCity, distance));
			inserted = true;
		}
		return inserted;
	}
	
	/**
	 * This method calls the {@link #insertRoad(int, int, int)} method, using the {@link #getCityNumberFromCityCode(String)} method
	 * to find the city codes of the passed parameters.
	 * 
	 * @param fromCity - A two-character city code of the origin city.
	 * @param toCity - A two-character city code of the destination city.
	 * @param distance - An interger representation of distance.
	 * @return True if road was inserted, false if the road already exists.
	 */
	public boolean insertRoad(String fromCity, String toCity, int distance){
		return insertRoad(getCityNumberFromCityCode(fromCity), getCityNumberFromCityCode(toCity), distance);
	}
	
	/**
	 * This method calls the {@link #deleteRoad(int, int)} method by finding the city numbers of the passed city codes.
	 * 
	 * @param fromCity - Two-character city code of the origin city.
	 * @param toCity - Two-character city code of the destination city.
	 * @return True if deleted, false if the road doesn't exist.
	 */
	public boolean deleteRoad(String fromCity, String toCity){
		return deleteRoad(getCityNumberFromCityCode(fromCity), getCityNumberFromCityCode(toCity));
	}
	
	/**
	 * This method removes a road from the roads array.
	 * 
	 * @param fromCity
	 * @param toCity
	 * @return True if a road was deleted, false if no road was found to delete.
	 */
	public boolean deleteRoad(int fromCity, int toCity){
		boolean deleted = false;
		Iterator<Road> iter = roads.iterator();
		
		while(iter.hasNext()){
			Road road = iter.next();
			
			if((road.getFromCity() == fromCity) && (road.getToCity() == toCity)){
				iter.remove();
				deleted = true;
			}
		}
		return deleted;
	}
	
	/**
	 * 
	 * @param cityCode - A two-character city code.
	 * @return The city object of the specified city if found, null if not found.
	 */
	public City getCityObject(String cityCode){
		City cityObject = null;
		for(City city: cities){
			if(city.getCityCode().equals(cityCode)){
				cityObject = city;
			}
		}
		return cityObject;
	}
	
	public String getCityInformation(String cityCode){
		City desiredCity = getCityObject(cityCode);
		String output = "";
		
		if(desiredCity == null){
			// city not found
			output = "No such city found";
		}else{
			output = desiredCity.getCityNumber() + " " + desiredCity.getCityCode() + " " + desiredCity.getFullCityName() + " " + desiredCity.getPopulation() + " " + desiredCity.getElevation();
		}
		return output;
	}
}

class City{
	/**
	 * This field represents the unique number of the city.
	 */
	private int cityNumber;
	
	/**
	 * This field represents the unique two-character code of the city.
	 */
	private String cityCode;
	
	/**
	 * This field represents the full name of the city.
	 */
	private String fullCityName;
	
	/**
	 * This field represents the population of the city.
	 */
	private int population;
	
	/**
	 * This field represents the elevation of the city.
	 */
	private int elevation;
	
	/**
	 * This constructor creates a City object based on the passed parameters.
	 * 
	 * @param cityNumber - The number of the city. Must be unique.
	 * @param cityCode - The two-character code of the city. Must be unique.
	 * @param fullCityName - The full name of the city.
	 * @param population - The population of the city.
	 * @param elevation - The elevation of the city.
	 */
	public City(int cityNumber, String cityCode, String fullCityName, int population, int elevation){
		this.cityNumber = cityNumber;
		this.cityCode = cityCode;
		this.fullCityName = fullCityName;
		this.population = population;
		this.elevation = elevation;
	}
	
	public int getCityNumber() {
		return cityNumber;
	}

	public void setCityNumber(int cityNumber) {
		this.cityNumber = cityNumber;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getFullCityName() {
		return fullCityName;
	}

	public void setFullCityName(String fullCityName) {
		this.fullCityName = fullCityName;
	}

	public int getPopulation() {
		return population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}
}

class Road{
	/**
	 * This field represents a city number of a {@link City}. This is the origin city of the road.
	 */
	private int fromCity;
	
	/**
	 * This field represents a city number of a {@link City}. This is the destination city of the road.
	 */
	private int toCity;
	
	/**
	 * This field represents the distance of the road. This is used as the weight.
	 */
	private int distance;
	
	/**
	 * This constructor creates a Road object based on the parameters specified.
	 * 
	 * @param fromCity - City number of the origin city.
	 * @param toCity - City number of the destination city.
	 * @param distance - Distance between the cities.
	 */
	public Road(int fromCity, int toCity, int distance){
		this.fromCity = fromCity;
		this.toCity = toCity;
		this.distance = distance;
	}

	public int getFromCity() {
		return fromCity;
	}

	public void setFromCity(int fromCity) {
		this.fromCity = fromCity;
	}

	public int getToCity() {
		return toCity;
	}

	public void setToCity(int toCity) {
		this.toCity = toCity;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
}

/**
 * This class represents a min heap. The heap is internally represented as an array of integers.
 * The min heap can have elements inserted into it, or it can be build from an existing array of
 * integer values.  Elements can be removed from the heap. The heap can also be returned as an array of integers.
 * 
 * @author Samuel Holt
 *
 */
class MinHeap
{
	/**
	 * This field is the heap represented as an array of integers.
	 */
    private int[] heap;
    
    /**
     * This is the current position of the array where there is an empty space to place an entry.
     */
    private int currentSize;
    
    /**
     * This is the maximum possible capacity of the heap.
     */
    private int capacity;
    
    /**
     * This is the number of swaps it takes to build the heap using the optimal method.
     */
    private int optimalSwaps;
    
    /**
     * This is the number of swaps it takes to build the heap using series insertions.
     */
    private int normalSwaps;
    
    /**
     * This is the default value used for capacity if no value is provided.
     */
    private static int defaultCapacity = 100;
    
    /**
     * This is the default constructor for the MaxHeap.  It uses the value
     * of {@link #defaultCapacity}.
     */
    public MinHeap(){
    	this(defaultCapacity);
    }
    
    /**
     * This constructor builds a heap based on a passed capacity parameter.  
     * 
     * @param capacity - The size of the heap.
     */
    public MinHeap(int capacity)
    {
        this.capacity = capacity + 1;
        this.currentSize = 0;
        this.heap = new int[this.capacity + 1];
    }
    
    /**
     * This constructor builds a heap based on an array of integers.
     * It builds the heap based on either series insertions or 
     * the optimal method depending on the passed value for optimal.
     * 
     * @param maxHeap - An array of integers to build a max heap from.
     * @param optimal - Use the optimal method to build the heap if true, otherwise use series insertions.
     */
    public MinHeap(int[] maxHeap, boolean optimal){
    	if(optimal){
    		this.capacity = maxHeap.length;
        	this.currentSize = this.capacity;
        	this.heap = new int[this.capacity + 1];
    		System.arraycopy(maxHeap, 0, this.heap, 0, maxHeap.length);
        	minHeap();
    	}else{
    		this.capacity = maxHeap.length;
            this.currentSize = 0;
            this.heap = new int[this.capacity + 1];
            
    		for(int i = 1; i < maxHeap.length; i++){
    			this.insert(maxHeap[i]);
    		}
    		
    	}
    	
    }
 
    /**
     * @param position - Position of element in the array.
     * @return The index of the array of the parent of the passed position.
     */
    private int parent(int position)
    {
        return position / 2;
    }
 
    /**
     * 
     * @param position - Position of element in the array.
     * @return The index of the left child of the passed position in the array.
     */
    private int leftChild(int position)
    {
        return (2 * position);
    }
 
    /**
     * 
     * @param position - Position of element in the array.
     * @return The index of the right child of the passed position in the array.
     */
    private int rightChild(int position)
    {
        return (2 * position) + 1;
    }
    
    /**
     * @param position - A position of an element in the array.
     * @return True if the given position is a leaf node, false if not.
     */
    private boolean isLeaf(int position){
    	return !hasLeftChild(position) && !hasRightChild(position); 
    }
    
    /**
     * @param position - A position of an element in the array.
     * @return True if the element has a left child, false if not.
     */
    private boolean hasLeftChild(int position){
    	boolean hasLeftChild = true;
    	try{
    		// try to assign x to the value of the left child of the 
    		// passed position
    		int x = heap[leftChild(position)];
    	}catch(ArrayIndexOutOfBoundsException exception){
    		// this means there is no defined left child of the element
    		hasLeftChild = false;
    	}
    	return hasLeftChild;
    }
    /**
     * 
     * @param position - A position of an element in the array.
     * @return True if the element has a right child, false if not.
     */
    private boolean hasRightChild(int position){
    	boolean hasRightChild = true;
    	try{
    		// try to assign x to the value of the right child of
    		// the passed position
    		int x = heap[rightChild(position)];
    	}catch(ArrayIndexOutOfBoundsException exception){
    		// no defined right child of the element
    		hasRightChild = false;
    	}
    	
    	return hasRightChild;
    }
 
    /**
     * This method swaps the value of the passed first 
     * position with the value of the passed second position.
     * 
     * 
     * @param firstPosition - The first position to swap.
     * @param secondPosition - The second position to swap.
     */
    private void swap(int firstPosition,int secondPosition)
    {
        int tmp;
        tmp = heap[firstPosition];
        heap[firstPosition] = heap[secondPosition];
        heap[secondPosition] = tmp;
    }
 
    /**
     * This method builds a max heap from the passed position.
     * 
     * @param position - A position in the array.
     */
    private void reheap(int position)
    {
    	// if leaf node, no need to continue
        if (!isLeaf(position))
        { 
        	// determine if current position is larger than one of its child nodes
            if ( heap[position] > heap[leftChild(position)]  || heap[position] > heap[rightChild(position)])
            {
            	// determine which child is smaller, swap it, and perform a reheap on that child
                if (heap[leftChild(position)] < heap[rightChild(position)])
                {
                    swap(position, leftChild(position));
                    optimalSwaps++;
                    reheap(leftChild(position));
                }else
                {
                    swap(position, rightChild(position));
                    optimalSwaps++;
                    reheap(rightChild(position));
                }
            }
        }
    }
 
    /**
     * This method inserts an element into the heap and swaps until
     * the max heap rules are satisfied.
     * 
     * @param element - An integer element to insert into the heap.
     */
    public void insert(int element)
    {
    	currentSize++;
        heap[currentSize] = element;
        int current = currentSize;
        
        // Swap the heap with the parent node until the current node is smaller than the parent
        while(heap[current] < heap[parent(current)] && parent(current) != 0)
        {
            swap(current,parent(current));
            normalSwaps++;
            current = parent(current);
        }	
    }
    
    /**
     * This method prints the first passed number of elements in the array. 
     * 
     * @param numberOfElements - Number of elements to print.
     * @return A string representation of the first passed amount of elements in the array.
     */
    public String print(int numberOfElements){
    	String output = "";
    	for(int i = 1; i <= numberOfElements; i++){
    		output += this.heap[i] + ",";
    	}
    	output += "...";
    	return output;
    }
 
    /**
     * This method provides a string representation of the heap. This allows for an easy
     * visualization of the heap.
     */
    @Override
    public String toString()
    {
    	String output = "";
    	
        for (int i = 1; i <= (currentSize / 2); i++ )
        {
        	if(!isLeaf(i)){
        		output += " Parent Node : " + heap[i] + " Left Child : " + heap[2*i]
                        + " Right Child :" + heap[2 * i  + 1] + "\r\n";
        	}else{
        		output += " Leaf Node: " + heap[i] + "\r\n";
        	}
        	
        }
        
        return output;
    }
 
    /**
     * This method builds a max heap out of the current heap array.
     */
    public void minHeap()
    {
        for (int position = (currentSize / 2); position >= 1; position--)
        {
            reheap(position);
        }
    }
 
    /**
     * This method removes the top element from the heap. In other words, this method performs
     * a pop on the heap.
     * 
     * @return The value of the top element in the heap.
     */
    public int remove()
    {
        int first = heap[1];
        heap[1] = heap[currentSize]; 
        reheap(1);
        return first;
    }
    
    /**
     * This method performs the specified amount of removals on the heap.
     * 
     * @param amount - Amount of elements to remove.
     */
    public void remove(int amount){
    	for(int i = 0; i < amount; i++){
    		remove();
    	}
    }
    
    /**
     * 
     * @return The amount of swaps used to build the heap by the series insertion method.
     */
    public int getNormalSwaps(){
    	return this.normalSwaps;
    }
    
    /**
     * 
     * @return The amount of swaps used to build the heap by the optimal method.
     */
    public int getOptimalSwaps(){
    	return this.optimalSwaps;
    }
    
    /**
     * 
     * @return The heap as an array of integers.
     */
    public int[] getHeapArray(){
    	return this.heap;
    }
}