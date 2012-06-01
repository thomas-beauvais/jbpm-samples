[condition][driver]There is a Driver=driverVar : Driver()
[condition][driver]- age less than {age} years old=age < {age}
[condition][driver]- age greater than {age} years old=age > {age}
[condition][driver]- has had more than {number} number of accidents=numberOfAccidents > {number}
[condition][driver]- age is at least {age}=age >= {age}
[condition][driver]- age is between {lower} and {upper} years old=age >= {lower}, age <= {upper}
[condition][driver]- has had exactly {number} number of accidents=numberOfAccidents == {number}
[condition][driver]- has more than {number} number of tickets=numberOfTickets > {number}
[condition][driver]- has less than or equal to {number} number of tickets=numberOfTickets <= {number}
[condition][policy]There is a Policy=policyVar : Policy()
[condition][policy]- policy has a driver {driver}=driver == driverVar
[condition][policy]- policy type is '{type}'=policyType ==  "{type}"
[condition][policy]- vehicle year is greater than {year}=vehicleYear > {year}
[condition][policy]- price is greater than {amount}=price > {amount}
[condition][policy]- price is equal to {amount}=price == {amount}
[consequence][]Reject Policy with explanation : '{reason}'=insert(new Rejection("{reason}"));
[condition][]Policy has not been rejected=not Rejection()
[consequence][]logRule=System.out.println("the rule that executed is: " + drools.getRule());
[consequence][policy]Add surcharge {surcharge} to Policy=modify(policyVar) \{price = policyVar.price + {surcharge}\}
[condition][]Sum all policies for the same driver=Number( total : intValue > 0) from accumulate( Policy( policyType != "MASTER",	 priceVar : price > 0, driver == driverVar), sum( priceVar ) )
[consequence][]Set policy total=modify (policyVar) {setPrice(total)}
