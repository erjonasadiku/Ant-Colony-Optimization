import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
public class House_Price {
    public String s = "";
    private double alpha = 3;
    private double beta = 2;
    private double Q = 2;
    private double antFactor = 10;
    private double randomFactor = 10;
    private int noOfRooms;
    private int maxIterations = 10;
    private int numberOfRooms;
    private int numberOfAnts;
    private double graph[][];
    private double trails[][];
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private double probabilities[];
    private int currentIndex;
    private int[] bestTourOrder;
    private double bestTourLength;

    public House_Price(double Number_of_Bedrooms, double Number_of_Bathrooms, double  Number_of_Kitchens, double  Number_of_Living_rooms, double  Number_of_Corridors, double  Number_of_Balconies, double  Number_of_Laundry_rooms, int  Number_of_Pantries, int  Number_of_Wardrobes) {

        noOfRooms = (int)( Number_of_Bedrooms + Number_of_Bathrooms +  Number_of_Kitchens + Number_of_Living_rooms + Number_of_Corridors + Number_of_Balconies + Number_of_Laundry_rooms + Number_of_Pantries + Number_of_Wardrobes);
        graph = generateRandomMatrix(noOfRooms);
        numberOfRooms = noOfRooms;
        numberOfAnts = (int) (numberOfRooms * antFactor);
        trails = new double[numberOfRooms][numberOfRooms];
        probabilities = new double[numberOfRooms];
        for(int i = 0; i < numberOfAnts; i++)
            ants.add(new Ant(numberOfRooms));
    }
    public double[][] generateRandomMatrix(int n)
    {
        double[][] randomMatrix = new double[n][n];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++)
            {
                if(i == j)
                    randomMatrix[i][j] = 0;
                else
                    randomMatrix[i][j] = Math.abs(random.nextInt(100) + 1);
            }
        }

        s+=("\t");
        for(int i = 0; i < n; i++) // shtyp numrat rendor
            s+=("Room"+i+"\t");
        s+="\n";
        for(int i = 0; i < n; i++) // shtyp matricen me distance
        {
            s+=("Room"+i+"\t");
            for(int j = 0; j < n; j++)
                s+=(randomMatrix[i][j]+"\t");
            s+="\n";
        }
        int sum=0;
        for(int i = 0; i < n-1; i++)
            sum+=randomMatrix[i][i+1];
        sum+=randomMatrix[n-1][0];
        s+=("\nNaive solution 0-1-2-...-n-0 = " + sum + " \n");
        return randomMatrix;
    }
    public void startAntOptimization()
    {
        for(int i = 1; i <= maxIterations; i++)
        {
            s+=("\nAttempt #" + i);
            solve();
            if(i == 10)
            {
                s+="\n";
                s+=("\nMinimal House Price: " + (bestTourLength - numberOfRooms) * 800 + "$");
            }
            s+="\n";
        }
    }
    public int[] solve()
    {
        setupAnts();
        clearTrails();
        for(int i = 0; i < maxIterations; i++)
        {
            moveAnts();
            updateTrails();
            updateBest();
        }
        s+=("\nBest tour length: " + (bestTourLength - numberOfRooms));
        s+=("\nBest tour order: " + Arrays.toString(bestTourOrder));
        return bestTourOrder.clone();
    }
    private void setupAnts()
    {
        for(int i = 0; i < numberOfAnts; i++)
        {
            for(Ant ant:ants)
            {
                ant.clear();
                ant.visitRoom(-1, random.nextInt(numberOfRooms));
            }
        }
        currentIndex = 0;
    }
    private void moveAnts()
    {
        for(int i = currentIndex; i < numberOfRooms-1; i++)
        {
            for(Ant ant:ants)
            {
                ant.visitRoom(currentIndex,selectNextRoom(ant));
            }
            currentIndex++;
        }
    }
    private int selectNextRoom(Ant ant)
    {
        int t = random.nextInt(numberOfRooms - currentIndex);
        if (random.nextDouble() < randomFactor)
        {
            int roomIndex=-999;
            for(int i = 0; i < numberOfRooms; i++)
            {
                if(i == t && !ant.visited(i))
                {
                    roomIndex = i;
                    break;
                }
            }
            if(roomIndex!=-999)
                return roomIndex;
        }
        calculateProbabilities(ant);
        double r = random.nextDouble();
        double totNumber_of_Bathroom = 0;
        for (int i = 0; i < numberOfRooms; i++)
        {
            totNumber_of_Bathroom += probabilities[i];
            if (totNumber_of_Bathroom >= r)
                return i;
        }
        throw new RuntimeException("There are no other romms.");
    }
    public void calculateProbabilities(Ant ant)
    {
        int i = ant.trail[currentIndex];
        double pheromone = 0.0;
        for (int l = 0; l < numberOfRooms; l++)
        {
            if (!ant.visited(l))
                pheromone += Math.pow(trails[i][l], alpha) * Math.pow(1.0 / graph[i][l], beta);
        }
        for (int j = 0; j < numberOfRooms; j++)
        {
            if (ant.visited(j))
                probabilities[j] = 0.0;
            else
            {
                double numerator = Math.pow(trails[i][j], alpha) * Math.pow(1.0 / graph[i][j], beta);
                probabilities[j] = numerator / pheromone;
            }
        }
    }
    private void updateTrails()
    {
        for (Ant a : ants)
        {
            double contribution = Q / a.trailLength(graph);
            for (int i = 0; i < numberOfRooms - 1; i++)
                trails[a.trail[i]][a.trail[i + 1]] += contribution;
            trails[a.trail[numberOfRooms - 1]][a.trail[0]] += contribution;
        }
    }
    private void updateBest()
    {
        if (bestTourOrder == null)
        {
            bestTourOrder = ants.get(0).trail;
            bestTourLength = ants.get(0).trailLength(graph);
        }
        for (Ant a : ants)
        {
            if (a.trailLength(graph) < bestTourLength)
            {
                bestTourLength = a.trailLength(graph);
                bestTourOrder = a.trail.clone();
            }
        }
    }
    private void clearTrails()
    {
        for(int i = 0; i < numberOfRooms; i++)
        {
            for(int j = 0; j < numberOfRooms; j++)
                trails[i][j] = 1;
        }
    }
}
