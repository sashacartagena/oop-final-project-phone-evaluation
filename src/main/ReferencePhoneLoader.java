package main;

import java.util.List;

public final class ReferencePhoneLoader {
    private ReferencePhoneLoader() {}

    public static void loadInto(PhoneList phoneList) {
        List<PhoneKnowledgeBase.PhoneReference> references = PhoneKnowledgeBase.getReferences();
        for (int i = 0; i < references.size(); i++) {
            PhoneKnowledgeBase.PhoneReference reference = references.get(i);
            phoneList.addPhone(new Phone(
                reference.getModel(),
                reference.getStorage(),
                "Reference market listing",
                reference.getMarketPrice()
            ));
        }
    }
}
