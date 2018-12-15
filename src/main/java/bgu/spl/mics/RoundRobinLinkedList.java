package bgu.spl.mics;

import java.util.LinkedList;

public class RoundRobinLinkedList <T>  extends LinkedList <T> {
    private int nextIndex = 0 ;

    public void updateIndexAfterRemove(int indexOfAction) {
        if(indexOfAction < nextIndex){
            nextIndex -- ;
        }
        else if (indexOfAction == nextIndex){
            step();
        }
        System.out.println("After remove , nextIndex is : "+nextIndex);
    }


    public T getNext(){
        if(size() > 0 && nextIndex >-1) {
            T value = get(nextIndex);
            step();
            System.out.println("After step function , nextIndex is : " + nextIndex);
            return value;
        }
        else{
            return null;
        }
    }

    private void step(){

            if(nextIndex < size() -1){
                nextIndex++;
            }
            else if(size() == 0 ){
            nextIndex = -1;
        }
            else{
                nextIndex = 0;
            }
    }

    public int getNextIndex() {
        return nextIndex;
    }

    @Override
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        boolean answer = super.remove(o);
        updateIndexAfterRemove(index);
        return answer;
    }

    public void printLinkedList(){
        for (int i = 0; i < size(); i++) {
            System.out.println(get(i));
        }
        System.out.println("Next index : "+nextIndex);
    }
}
