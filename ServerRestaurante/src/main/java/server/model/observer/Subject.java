package server.model.observer;

import java.util.LinkedList;


public abstract class Subject implements Observable{

    protected LinkedList<Observer> observers;

    protected Subject(){ this.observers=new LinkedList<>(); }

    @Override
    public void attach(Observer o) {
        this.observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        this.observers.forEach(Observer::update);
    }
}
