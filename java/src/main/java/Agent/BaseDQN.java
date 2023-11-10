package Agent;

import java.util.Random;

import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.mxnet.engine.MxEngine;
import ai.djl.mxnet.engine.MxGradientCollector;
import ai.djl.ndarray.NDManager;
import ai.djl.nn.Parameter;
import ai.djl.training.GradientCollector;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.util.Pair;
import ai.djl.util.PairList;
import Agent.Model.ScoreModel;
import Utils.Memory;

public abstract class BaseDQN extends BaseAgent {
    protected static final float MIN_EXPLORE_RATE = 0.1f;
    protected static final float DECAY_EXPLORE_RATE = 0.99f;

    protected final Random random = new Random(0);
    protected final Memory memory = new Memory(4096);

    private final int dim_of_state_space;
    private final int num_of_actions;
    private final int hidden_size;
    private final float learning_rate;

    protected final int batch_size;
    protected final int sync_net_interval;
    protected final float gamma;

    // private GradientCollector collector;
    private Optimizer optimizer;
    private Model policy_net;
    private Model target_net;

    protected NDManager manager;
    protected Predictor<NDList, NDList> policy_predictor;
    protected Predictor<NDList, NDList> target_predictor;

    private int iteration = 0;
    protected float epsilon = 1.0f;

    protected BaseDQN(int dim_of_state_space, int num_of_actions, int hidden_size, int batch_size,
            int sync_net_interval, float gamma, float learning_rate) {
        this.dim_of_state_space = dim_of_state_space;
        this.num_of_actions = num_of_actions;
        this.hidden_size = hidden_size;
        this.batch_size = batch_size;
        this.sync_net_interval = sync_net_interval;
        this.gamma = gamma;
        this.learning_rate = learning_rate;
        reset();
    }

    @Override
    public final int react(float[] state) {
        int action;
        try (NDManager submanager = manager.newSubManager()) {
            if (!isEval()) {
                memory.setState(state);
                if (memory.size() > batch_size) {
                    updateModel(submanager);
                }
            }

            action = getAction(submanager, state);
            if (!isEval()) {
                memory.setAction(action);
            }

        } catch (TranslateException e) {
            throw new IllegalStateException(e);
        }

        return action;
    }

    @Override
    public final void collect(float reward, boolean done) {
        if (!isEval()) {
            memory.setRewardAndMask(reward, done);
        }
    }

    @Override
    public final void reset() {
        optimizer = Optimizer.adam().optLearningRateTracker(Tracker.fixed(learning_rate)).build();

        if (manager != null) {
            manager.close();
        }
        manager = NDManager.newBaseManager();
        policy_net = ScoreModel.newModel(manager, dim_of_state_space, hidden_size, num_of_actions);
        target_net = ScoreModel.newModel(manager, dim_of_state_space, hidden_size, num_of_actions);

        policy_predictor = policy_net.newPredictor(new NoopTranslator());
        syncNets();
    }

    protected final void syncNets() {
        for (Pair<String, Parameter> params : policy_net.getBlock().getParameters()) {
            try {
                target_net.getBlock().getParameters().get(params.getKey())
                        .setArray(params.getValue().getArray().duplicate());
            } catch (Exception ex) {

            }
        }

        target_predictor = target_net.newPredictor(new NoopTranslator());
    }

    protected final void gradientUpdate(NDArray loss) {
        if (!MxGradientCollector.isRecording()) {
            MxGradientCollector.setRecording(true);
        }
        try (GradientCollector collector = MxEngine.getInstance().newGradientCollector();) {
            collector.backward(loss);
            for (Pair<String, Parameter> params : policy_net.getBlock().getParameters()) {
                NDArray params_arr = params.getValue().getArray();
                if (params_arr.getGradient() != null) {
                    optimizer.update(params.getKey(), params_arr, params_arr.getGradient());
                }
            }
            // MxGradientCollector.setRecording(false);
            // collector.close();
        } catch (Exception ex) {
            // System.out.println(ex);
        }
        if (iteration++ % sync_net_interval == 0) {
            epsilon *= DECAY_EXPLORE_RATE;
            syncNets();
        }
    }

    protected abstract int getAction(NDManager manager, float[] state) throws TranslateException;

    protected abstract void updateModel(NDManager manager) throws TranslateException;
}