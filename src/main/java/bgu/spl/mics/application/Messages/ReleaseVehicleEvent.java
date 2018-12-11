package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class ReleaseVehicleEvent implements Event {
    DeliveryVehicle tustusToRelease;
    public ReleaseVehicleEvent(DeliveryVehicle toRelease){this.tustusToRelease=toRelease;}
    public DeliveryVehicle getVehicle(){return tustusToRelease;}
}
