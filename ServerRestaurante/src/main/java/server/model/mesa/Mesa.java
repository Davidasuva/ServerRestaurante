package server.model.mesa;

import java.io.Serializable;

public class Mesa implements Serializable, Comparable<Mesa>{

    private int id;

    public Mesa(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj){
            return true;
        }
        if(obj==null || getClass()!=obj.getClass()){
            return false;
        }

        Mesa mesa = (Mesa) obj;
        return this.id == mesa.getId();
    }

    @Override
    public int compareTo(Mesa o) {
        return Integer.compare(this.id, o.getId());
    }

    @Override
    public String toString() {
        return "Mesa con id: "+id;
    }
}
