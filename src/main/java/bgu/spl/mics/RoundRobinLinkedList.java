package bgu.spl.mics;

import java.util.LinkedList;

public class RoundRobinLinkedList <T>  extends LinkedList <T> {
    private int nextIndex = 0 ;

    public void updateIndexAfterRemove(int indexOfAction) {
        if(indexOfAction < nextIndex){
            nextIndex -- ;
        }
    }


    public T getNext(){
        T value = get(nextIndex);
        step();
        return value;
    }

    private void step(){
            if(nextIndex < size() -1){
                nextIndex++;
            }
            else{
                nextIndex = 0;
            }
    }

    public int getNextIndex() {
        return nextIndex;
    }
}
