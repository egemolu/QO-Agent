import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agents.anac.y2010.Southampton.SouthamptonAgent;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

public class QOAgent extends AbstractNegotiationParty {

    private static final int DATABASE_AGENT_NUMBER = 100;

    private Bid lastReceivedBid = null;
    private Bid[] allBidsArray;
    private Double[] ourSortedBidArr;
    private Double[] oppSortedBidArr;
    private Double[][] QOValueArray;
    private ArrayList<Bid> allBidsList = new ArrayList<>();
    private List<AgentForQO> sampleAgents;

    private int selectedAgent = 0;
    private int bidCount = 0;
    private double threshHoldValue = 0.05;
    private double reservationValue;

    private boolean debug_mode = false;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        BidIterator iterator = new BidIterator(utilitySpace.getDomain());
        calculateBidCount(iterator);
        AgentGeneratorV2ForQO agentGenerator = new AgentGeneratorV2ForQO(DATABASE_AGENT_NUMBER, utilitySpace.getDomain().getIssues());
        sampleAgents = agentGenerator.getAgents();
        reservationValue = utilitySpace.getReservationValue();
        createAndSortOurArr();
        createAndSortOppArr();
        calculateQOValues();
    }

    public Bid generateBid() {
        int index = findMaxQOIndex();
        return allBidsArray[index];
    }

    public int findMaxQOIndex() {
        int max = 0;
        double value = 0;
        for (int i = 0; i < QOValueArray.length; i++) {
            if (debug_mode) {
                System.out.println("Value = " + value);
                System.out.println("QOValueArray[i][0] = " + QOValueArray[i][0] + " index = " + i);
                System.out.println("------------------------------");
            }
            if (value < QOValueArray[i][0]) {
                if (debug_mode) {
                    System.out.println("Value Updated");
                    System.out.println("Max Updated");
                }
                value = QOValueArray[i][0];
                max = i;
            }
        }
        if (debug_mode) {
            System.out.println("Max index" + max);
        }

        return max;
    }

    private void calculateQOValues() {
        for (int i = 0; i < bidCount; i++)
            QOValueArray[i][0] = QOValue(allBidsArray[i]);
    }


    public double QOValue(Bid offer) {
        double alpha = alphaValue(offer);
        double beta = betaValue(offer);
        if (debug_mode) {
            System.out.println("Alpha value comes from alphaValue Method " + alpha);
            System.out.println("Beta value comes from betaValue Method " + beta);
            System.out.println("Min of alpha and beta " + Math.min(alpha, beta));
            System.out.println("--------------------");
        }
        return Math.min(alpha, beta);
    }

    public double alphaValue(Bid offer) {
        if (debug_mode) {
            System.out.println("Offer rank for us from calculateOfferRank method " + calculateOfferRank(offer, false));
            System.out.println("Luce Number for us from calculateLuceNumber method " + calculateLuceNumber(offer, false));
        }
        return (calculateOfferRank(offer, false) * calculateLuceNumber(offer, false));
    }

    public double calculateOfferRank(Bid offer, boolean isOpp) {
        double util = (isOpp) ? calculateOppUtil(offer, -1) : utilitySpace.getUtility(offer);
        for (int i = 0; i < bidCount; i++) {
            if (isOpp) {
                if (oppSortedBidArr[i] == util)
                    return (double) (i + 1) / bidCount;
            } else {
                if (ourSortedBidArr[i] == util)
                    return (double) (i + 1) / bidCount;
            }
        }
        return -1;
    }

    public double calculateLuceNumber(Bid offer, boolean isOpp) {
        double util = (isOpp) ? calculateOppUtil(offer, -1) : utilitySpace.getUtility(offer);
        if (debug_mode)
            System.out.println("Util in calculate Luce Number " + util);

        double sumOfUtils = 0;
        for (int i = 0; i < bidCount; i++)
            sumOfUtils += (isOpp) ? calculateOppUtil(allBidsArray[i], -1) : utilitySpace.getUtility(allBidsArray[i]);

        return util / sumOfUtils;
    }

    public double betaValue(Bid offer) {
        if (debug_mode) {
            System.out.println("Luce Number for us from calculateLuceNumber method " + calculateLuceNumber(offer, false));
            System.out.println("Luce Number for opp from calculateLuceNumber method " + calculateLuceNumber(offer, true));
            System.out.println("Offer rank for opp from calculateOfferRank method " + calculateOfferRank(offer, true));
        }
        return (calculateLuceNumber(offer, false) + calculateLuceNumber(offer, true)) * calculateOfferRank(offer, true);
    }

    public void calculateBidCount(BidIterator iterator) {
        while (iterator.hasNext()) {
            Bid bid = iterator.next();
            allBidsList.add(bid);
            bidCount++;
        }
        if (debug_mode) {
            System.out.println("Bid Count= " + bidCount);
        }
    }

    public void createAndSortOurArr() {
        allBidsArray = new Bid[bidCount];
        QOValueArray = new Double[bidCount][1];
        ourSortedBidArr = new Double[bidCount];
        for (int i = 0; i < bidCount; i++) {
            allBidsArray[i] = allBidsList.get(i);
            ourSortedBidArr[i] = utilitySpace.getUtility(allBidsList.get(i));
        }
        quickSort(ourSortedBidArr, 0, bidCount - 1);
        if (debug_mode) {
            for (double d : ourSortedBidArr) {
                System.out.println("Our sorted Bid array sorting " + d);
            }
            System.out.println("-------------------");
        }
    }

    public void createAndSortOppArr() {
        oppSortedBidArr = new Double[bidCount];
        for (int i = 0; i < bidCount; i++) {
            oppSortedBidArr[i] = calculateOppUtil(allBidsList.get(i), -1);
        }
        quickSort(oppSortedBidArr, 0, bidCount - 1);
        if (debug_mode) {
            for (double d : oppSortedBidArr) {
                System.out.println("Opp sorted Bidd array sorting " + d);
            }
            System.out.println("-------------------");
        }
    }



    public double calculateOppUtil(Bid offer, int agentNumber) {
        if (agentNumber == -1)
            return sampleAgents.get(selectedAgent).calculateUtility(offer);

        return sampleAgents.get(agentNumber).calculateUtility(offer);
    }

    public void updateAgentType() {
        HashMap<Integer, Double> luceMap = new HashMap<>();
        HashMap<Integer, Double> resultMap = new HashMap<>();
        int currentAgentId = selectedAgent;
        double maxProb = 0;

        double prob = 1.0 / sampleAgents.size();
        double denominator = 0;

        for (int i = 0; i < sampleAgents.size(); i++) {

            double util = calculateOppUtil(lastReceivedBid, i);
            double sumOfUtils = 0;
            for (int j = 0; j < bidCount; j++)
                sumOfUtils += (calculateOppUtil(allBidsArray[j], i));

            double luceNumber = util / sumOfUtils;
            luceMap.put(i, luceNumber);
        }

        for (Map.Entry<Integer, Double> entry : luceMap.entrySet()) {
            denominator += entry.getValue();
        }
        denominator *= prob;
        for (int k = 0; k < sampleAgents.size(); k++) {
            double result = (luceMap.get(k) * prob) / denominator;
            resultMap.put(k, result);
        }
        for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
            if (entry.getValue() > maxProb) {
                maxProb = entry.getValue();
                selectedAgent = entry.getKey();
            }
        }
        if (selectedAgent != currentAgentId) {
            if (debug_mode) {
                System.out.println("------------------");
                System.out.println("Agent Type Changed ");
                System.out.println("New Believed Type "+ selectedAgent);
                System.out.println("------------------");
            }
            createAndSortOppArr();
            calculateQOValues();

        }
    }

    @Override
    public Action chooseAction(List<Class<? extends Action>> validActions) {
        if (lastReceivedBid == null) {
            if (debug_mode)
                System.out.println("Current Believed Type: " + selectedAgent);
            return new Offer(getPartyId(), generateBid());

        }
        updateAgentType();
        Bid nextOffer = generateBid();
        double oppUtil = calculateOppUtil(lastReceivedBid, -1);
        double receivedBidUtil = utilitySpace.getUtility(lastReceivedBid);
        double nextRoundBidUtil = utilitySpace.getUtility(nextOffer);
        double nextRoundOppBidUtil = calculateOppUtil(nextOffer, -1);

        if (debug_mode) {
            System.out.println("Current Believed Type: " + selectedAgent);
            System.out.println("Our Generated bid utility from utilityspace: " + utilitySpace.getUtility(nextOffer));
            System.out.println("Last Received Bid For Us Utility from getUtility Method : " + receivedBidUtil);
            System.out.println("Last Received Bid For Opp Utility from calculateOppUtil Method : " + oppUtil);
            System.out.println("Next round bid utility for Opp from calculate opp util: " + nextRoundOppBidUtil);
        }

        if (receivedBidUtil >= nextRoundBidUtil) {
            if (debug_mode) {
                System.out.println("receivedBidUtil >= nextRoundBidUtil case ");
            }
            return new Accept(getPartyId(), lastReceivedBid);
        }
        if (Math.abs(nextRoundOppBidUtil - oppUtil) <= threshHoldValue) {
            if (debug_mode) {
                System.out.println("nextRoundOppBidUtil - oppUtil case ");
            }
            return new Offer(getPartyId(), generateBid());
        }


        double probAcceptance = calculateOfferRank(lastReceivedBid, false);
        double randomdNum = Math.random();

        if (receivedBidUtil >= reservationValue && randomdNum > probAcceptance) {
            if (debug_mode) {
                System.out.println("receivedBidUtil >= reservationValue && randomdNum > probAcceptance case ");
            }
            return new Accept(getPartyId(), lastReceivedBid);
        }
        if (debug_mode)
            System.out.println("Do not enter any case just offer");
        return new Offer(getPartyId(), generateBid());

    }

    @Override
    public void receiveMessage(AgentID sender, Action action) {
        super.receiveMessage(sender, action);
        if (action instanceof Offer) {
            lastReceivedBid = ((Offer) action).getBid();
        }
    }

    @Override
    public String getDescription() {
        return "QOAgent";
    }

    private int partition(Double arr[], int low, int high) {
        double pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                Double temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        Double temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }

    private void quickSort(Double arr[], int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }
}