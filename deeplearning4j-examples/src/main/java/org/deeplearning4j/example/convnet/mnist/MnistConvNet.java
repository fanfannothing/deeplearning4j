package org.deeplearning4j.example.convnet.mnist;

import org.apache.commons.math3.random.MersenneTwister;
import org.deeplearning4j.datasets.DataSet;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.datasets.mnist.draw.DrawMnistGreyScale;
import org.deeplearning4j.nn.Tensor;
import org.deeplearning4j.plot.FilterRenderer;
import org.deeplearning4j.plot.NeuralNetPlotter;
import org.deeplearning4j.rbm.ConvolutionalRBM;
import org.deeplearning4j.util.MatrixUtil;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MnistConvNet {

    private static Logger log = LoggerFactory.getLogger(MnistConvNet.class);

    public static void main(String[] args) throws Exception {

        ConvolutionalRBM r = new ConvolutionalRBM
                .Builder().withFilterSize(new int[]{7, 7})
                .withNumFilters(4).withStride(new int[]{2, 2})
                .withVisibleSize(new int[]{28, 28}).numberOfVisible(28).numHidden(28).useAdaGrad(false)

                .build();


        //batches of 10, 60000 examples total
        DataSetIterator iter = new MnistDataSetIterator(1,2);

        while(iter.hasNext()) {
            DataSet next = iter.next();
            DoubleMatrix reshape = next.getFirst().reshape(28,28);
            r.trainTillConvergence(reshape, 1e-3, new Object[]{1, 1e-3, 5000});

        }







        iter.reset();




        //Iterate over the data set after done training and show the 2 side by side (you have to drag the test image over to the right)
        while(iter.hasNext()) {
            DataSet first = iter.next();
            DoubleMatrix next = first.getFirst().reshape(28,28);
            Tensor W = (Tensor) r.getW().dup();
            DoubleMatrix draw =  W.reshape(W.rows() * W.columns(),W.slices());
            log.info("Draw sum " + draw.sum());
            FilterRenderer render = new FilterRenderer();
            render.renderFilters(draw,"tmpfile.png",7,7);
        }
    }

}