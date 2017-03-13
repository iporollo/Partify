package ip.partyplaylist.screen_actions;


import ip.partyplaylist.model.Party;

public interface CreatePartyScreenActions {

    void showError(String error);

    void showPartyCreatedScreen(Party createdParty);
}
