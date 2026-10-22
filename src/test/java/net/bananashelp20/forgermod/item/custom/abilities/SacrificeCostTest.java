package net.bananashelp20.forgermod.item.custom.abilities;

public final class SacrificeCostTest {
    public static void main(String[] args) {
        var example=SacrificeCost.pay(20,5);
        if(example.health()!=12.5f || example.absorption()!=0)throw new AssertionError(example);
        var mostlyAbsorption=SacrificeCost.pay(20,100);
        if(mostlyAbsorption.health()!=20 || mostlyAbsorption.absorption()!=40)throw new AssertionError(mostlyAbsorption);
        for(int health=1;health<100;health++)for(int absorption=0;absorption<100;absorption++) {
            var left=SacrificeCost.pay(health,absorption);
            if(left.health()<=0 || left.absorption()<0 || left.health()>health || left.absorption()>absorption
                    || left.health()+left.absorption()!=(health+absorption)*.5f)throw new AssertionError(left);
        }
        if(SacrificeCost.durationTicks(1)!=300 || SacrificeCost.durationTicks(4)!=600 || SacrificeCost.durationTicks(5)!=700)throw new AssertionError();
    }
}
