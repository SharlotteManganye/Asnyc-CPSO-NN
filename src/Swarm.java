import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class Swarm extends Thread{
    private int i,j,particles;
    private double inertia, context1, context2, maxVelocity, iterations;
    private double[] globalBestError;
    private double[] globalBestWeights;
    private double[][] personalBestError;
    private double[][] Velocities;
    private double[][] Weights;
    private double[][] personalBestWeights;
    private SimpleMatrix[] NeuralNetwork;
    private SimpleMatrix[] NN;
    private int[] config;

    Swarm(int i, int j, int particleSize, int[] config){
        this.i = i;
        this.j = j;
        this.config = config;
        particles= CPSO.particles;
        inertia = CPSO.inertia;
        context1 = CPSO.context1;
        context2 = CPSO.context2;
        maxVelocity = CPSO.maxVelocity;
        iterations = CPSO.iterations;
        globalBestError = new double[particleSize];
        globalBestWeights = new double[particleSize];
        this.Velocities = new double[particles][particleSize];
        this.Weights = new double[particles][particleSize];
        this.personalBestError = new double[particles][particleSize];
        this.personalBestWeights = new double[particles][particleSize];
        NeuralNetwork = new SimpleMatrix[config.length];
        for(int l =0; l < NeuralNetwork.length; l++){
            double[] nodeVals = new double[config[i]];
            Arrays.fill(nodeVals,0);
            NeuralNetwork[l] = new SimpleMatrix(1,config[l],true,nodeVals);
        }
        Arrays.fill(globalBestError,Double.MAX_VALUE);
        Arrays.fill(globalBestWeights, (Math.random() * (CPSO.maxW- CPSO.minW)) + CPSO.minW);
        for (double[] velocity : Velocities) {
            Arrays.fill(velocity, 0);
        }
        for (double[] values : Weights) {
            Arrays.fill(values, (Math.random() * (CPSO.maxW- CPSO.minW)) + CPSO.minW);
        }
        for(double[] values : personalBestError){
            Arrays.fill(values, Double.MAX_VALUE);
        }
        for (double[] values : personalBestWeights) {
            Arrays.fill(values, (Math.random() * (CPSO.maxW- CPSO.minW)) + CPSO.minW);
        }

    }

    private double activate(double input){
        double pve = Math.exp(input);
        double nve = Math.exp(-input);
        return (pve - nve)/(pve + nve);
    }

    private double[] feedForward(double[] input) {
        double[] output = new double[NeuralNetwork[NeuralNetwork.length-1].numCols()];
        for(int _n = 0; _n<NeuralNetwork[0].numCols(); _n++){
            NeuralNetwork[0].set(0,_n,input[_n]);
        }
        for(int l =0; l < NeuralNetwork.length-1; l++){
            for( int _n = 0; _n < NeuralNetwork[l].numCols(); _n++){
                NeuralNetwork[l].set(0,_n,activate(NeuralNetwork[l].get(0,_n)));
            }
            NeuralNetwork[l+1] = NeuralNetwork[l].mult(NN[l]);
        }

        for(int n = 0; n < NeuralNetwork[NeuralNetwork.length-1].numCols(); n++){
            output[n] = activate(NeuralNetwork[NeuralNetwork.length-1].get(0,n));
        }
        return output;
    }

    public void run(){
        List<double[]> lData = CPSO.getData();
        int dataSize = lData.size();
        for (int _i = 0 ; _i < iterations; _i++){
            for (int p =0; p < Weights.length; p++){
                for ( int w= 0; w <Weights[p].length; w++){
                    NN = CPSO.getNetwork();
                    NN[i].set(j,w,Weights[p][w]);
                    double mse = 0;
                    Collections.shuffle(lData);
                    for (double[] input: lData){
                        double[] output = feedForward(input);
                        double expected = input[input.length-1];
                        double error = 0;
                        for (int e = 0; e<output.length; e++){
                            if (e == expected){
                                error +=  ((1-output[e]) * (1-output[e]));
                            }else{
                                error +=  ((output[e]) * (output[e]));
                            }
                        }
                        error = error/output.length;
                        mse+=error;
                    }
                    mse = mse / dataSize;
                    if (personalBestError[p][w] > mse){
                        personalBestError[p][w] = mse;
                        personalBestWeights[p][w] = Weights[p][w];
                    }
                    if(globalBestError[w]>mse){
                        globalBestError[w] = mse;
                        globalBestWeights[w] = Weights[p][w];
                        CPSO.updateWeight(i,j,w,Weights[p][w],mse);
                    }
                    Velocities[p][w] = (inertia * Velocities[p][w]) + ((context1 * Math.random())*(globalBestWeights[w] - Weights[p][w])) + ((context2 * Math.random())*(personalBestWeights[p][w] - Weights[p][w]));
                    if (Velocities[p][w]>maxVelocity){
                        Velocities[p][w] = maxVelocity;
                    }
                    if (Velocities[p][w] < -maxVelocity){
                        Velocities[p][w] = -maxVelocity;
                    }
                    Weights[p][w] += Velocities[p][w];
                }
            }
        }
    }

}
