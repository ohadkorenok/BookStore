package bgu.spl.mics.application.passiveObjects;

public class OrderSchedule implements Comparable{

    private String bookTitle;
    private int tick;

    public OrderSchedule(String title, int tick) {
        this.bookTitle = title;
        this.tick = tick;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    @Override
    public int compareTo(Object o) {
        int answer=0;
        if(o instanceof OrderSchedule){
            answer = tick - ((OrderSchedule) o).getTick();
        }
        else{
            throw new ClassCastException("Not OrderSchedule Object");
        }
        return answer;
    }
}