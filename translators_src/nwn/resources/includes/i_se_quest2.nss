#include "i_se_structs"

// ============================ Quest constants ============================
// ScriptEase2 Quest pattern include file
//
// Author: ScriptEase Team
// =========================================================================

// TODO: remove these and replace with i_se_struct stuff

// Separates list elements
const string SEPARATOR = "_";
// appends the given element to the end of the given list
string SE2_AppendListElement(string list, string element);
// Gets the first element from the given array (array stays unmodified)
string SE2_GetFirstArrayElement(string array);
// removes first element from the given string array and returns the new array
string SE2_RemoveFirstArrayElement(string array);
// returns the number of elements in the given array
int SE2_ArraySize(string array);

// appends the given element to the end of the given list and returns the list
string SE2_AppendListElement(string list, string element) {
    if (list == "") {
        list = element;
    } else {
        list += SEPARATOR + element;
    }
    return list;
}

// Gets the first element from the given array (array stays unmodified)
string SE2_GetFirstArrayElement(string array) {
    string first = array;
    int index = FindSubString(array, SEPARATOR);
    if (index != -1) {
        // set left to be the left of the delimiter in string
        first = GetStringLeft(array, index);
    }
    // return the left of the delimiter
    //SendMessageToPC(GetFirstPC(), "element: " + first + " from array: " + array);
    return first;
}

// returns the number of elements in the given array
int SE2_ArraySize(string array) {
    if (array == "")
        return 0;

    int count = 1;
    int index = 0;
    while ((index = FindSubString(array, SEPARATOR, index)) != -1)
        count++;
    return count;
}

// removes first element from the given string array and returns the new array
string SE2_RemoveFirstArrayElement(string array) {
    string newString = "";
    int arrayLength = GetStringLength(array);
    int separatorLength = GetStringLength(SEPARATOR);
    int index = FindSubString(array, SEPARATOR);
    if (index != -1) {
        newString = GetStringRight(array, arrayLength - (index + separatorLength));
    }
    //SendMessageToPC(GetFirstPC(), "Before: " + array + " After: " + newString);
    return newString;
}


// ============================ Function Declarations ============================

// Determines if the questpoint with the given name has had its 
// fan-in count condition satisfied
//   name: The name of the quest point to test.
int SE2_Quest_FanInAchieved(string name);

// sets the questpoint and ancestor nodes to inactive, sets the questpoint to succeeded and activates the children points if they are able to be activated.
void SE2_Quest_SucceedQuestPoint(string name);

// sets the questpoint and ancestor nodes to inactive. sets the questpoint to failed. checks if the Quest can now not be succeeded.
void SE2_Quest_FailQuestPoint(string name);

// deactivates all of the ancestors of the given questpoint
void SE2_Quest_DeactivateAncestors(string name);

// Gets the given quest point's activation status as a boolean
//   oPlayer:   The owner of the quest point
//   name:      The name of the quest point to get active status for
int SE2_Quest_getState(object oPlayer, string name);

// Sets the given quest point's activation status as a boolean
//   oPlayer:   The owner of the quest point
//   name:      The name of the quest point to set active status for
//   bActive:   The new activation status 
int SE2_Quest_setState(object oPlayer, string name, int state);

// Registers the Quest Point with the given name in the quest system.
//   name:      The name of the quest point.
//   commiting: Boolean of whether the quest point elimitates other quest points or not.
//   fanIn:     The number of preceding quest points that must be completed prior.
//   player:    The player who owns the quest.
void SE2_Quest_RegisterQuestPoint(string name, int commiting, int fanIn, object player);

// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player);

// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player);

// Returns an array (string representation) containing the 
// names of all of the quest points that lead to the quest 
// point with the given name.
//   name: The name of the quest point whose precursors are to be found
string SE2_Quest_GetAllPreceding(string name);

// Returns an array (string representation) containing the 
// names of all of the quest points that follow from the 
// quest point with the given name.
//   name: The name of the quest point whose followers are to be found
string SE2_Quest_GetAllFollowing(string name);

// registers the given quest in the quest system
//void SE2_Quest_RegisterQuest(string name, string start, string end);

// Validates the quest system by checking if the quest cannot be succeeded
// This should be called after every Quest Point failure
//void SE2_Quest_ValidateQuest(string name);

// Disables all points in the quest
//void SE2_Quest_FailQuest(string name);

// Activates the quest�s start point
//void SE2_Quest_StartQuest(string name);

// ============================ Quest constants ============================
// The event number for quests.
const int SE2_QUEST_ENABLED_EVENT = 2012;

// State for a disabled Quest Point
const int QUEST_POINT_DISABLED = 0;
// State for an enabled Quest Point
const int QUEST_POINT_ENABLED = 1;
// State for a succeeded Quest Point
const int QUEST_POINT_SUCCESS = 2;
// State for a failed Quest Point
const int QUEST_POINT_FAIL = 3;

// suffix for variable to track quest point's current state.
const string QUEST_POINT_STATE = "_QuestPointState";
// Track if a Quest Point is committing
const string QUEST_POINT_COMMITING = "_QuestPointCommiting";
// Track the fanIn for a QuestPoint
const string QUEST_POINT_FANIN = "_QuestPointFanIn";
// Track the children for a QuestPoint
const string QUEST_POINT_CHILDREN = "_QuestPointChildren";
// Track the parents for a QuestPoint
const string QUEST_POINT_PARENTS = "_QuestPointParents";
// Track the containing Quest for a QuestPoint
const string QUEST_POINT_CONTAINER = "_QuestPointContainer";
// Track the start of a Quest
const string QUEST_START = "_QuestStart";
// Track the end of a Quest
const string QUEST_END = "_QuestEnd";
// Track if a Quest is currently active
const string QUEST_ACTIVE = "_QuestActive";


// ============================ Function Definitions ============================

// Determines if the questpoint with the given name has had its 
// fan-in count condition satisfied
//   name: The name of the quest point to test.
int SE2_Quest_FanInAchieved(string name) {
    //SendMessageToPC(GetFirstPC(), "Checking if " + name + " can be activated");
    object player = GetFirstPC();
    int fanIn = GetLocalInt(player, name + QUEST_POINT_FANIN);
    string parents = GetLocalString(player, name + QUEST_POINT_PARENTS);

    // for each parent
    while (SE2_ArraySize(parents) > 0) {
        string parent = SE2_GetFirstArrayElement(parents);
        parents = SE2_RemoveFirstArrayElement(parents);
        // has the parent succeeded?
        if (SE2_Quest_getState(player, parent) == QUEST_POINT_SUCCESS) {
            fanIn--;
        }
    }
	
    // return whether the fanIn condition has been satisfied
    return fanIn <= 0;
}

// Registers the Quest Point with the given name in the quest system.
//   name:      The name of the quest point.
//   commiting: boolean of whether the quest point elimitates other quest points or not.
//   fanIn:     The number of preceding quest points that must be completed prior.
//   player:    The player who owns the quest.
void SE2_Quest_RegisterQuestPoint(string name, int commiting, int fanIn, object player) {
    SE2_Quest_setState(player, name, QUEST_POINT_DISABLED);
    SetLocalInt(player, name + QUEST_POINT_COMMITING, commiting);
    SetLocalInt(player, name + QUEST_POINT_FANIN, fanIn);
	
    // set up QUEST_POINT_CONTAINER
    //setLocalString(player, name + QUEST_POINT_CONTAINER, containerQuest);
}

// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player) {
    string parents = GetLocalString(player, name + QUEST_POINT_PARENTS);
	
    parents = SE2_AppendListElement(parents, parent);
	
	// push to variable
    SetLocalString(player, name + QUEST_POINT_PARENTS, parents);
}

// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player) {
    // get the current list of children
    string children = GetLocalString(player, name + QUEST_POINT_CHILDREN);
    // append the child to it
    children = SE2_AppendListElement(children, child);
    // set up QUEST_POINT_CHILDREN
    SetLocalString(player, name + QUEST_POINT_CHILDREN, children);
}

//sets the questpoint and ancestor nodes to inactive, sets the questpoint to succeeded and activates the children points if they are able to be activated.
void SE2_Quest_SucceedQuestPoint(string name) {
    object player = GetFirstPC();
	
	SE2_Quest_setState(player, name, QUEST_POINT_SUCCESS);
	
    // get children and activate them
    string children = GetLocalString(player, name + QUEST_POINT_CHILDREN);
    //SendMessageToPC(GetFirstPC(), "Activating children: " + children);

    // for each child
    while (SE2_ArraySize(children) > 0) {
        string child = SE2_GetFirstArrayElement(children);
        //SendMessageToPC(GetFirstPC(), "Child: " + child);
        children = SE2_RemoveFirstArrayElement(children);
        //SendMessageToPC(GetFirstPC(), "Children: " + children);
        //SendMessageToPC(GetFirstPC(), "Trying to Activate : " + child);
		
        if (SE2_Quest_FanInAchieved(child)) {
            //SendMessageToPC(GetFirstPC(), "Child Activated");
			SE2_Quest_setState(player, name, QUEST_POINT_ENABLED);
			SignalEvent(GetModule(), EventUserDefined(SE2_QUEST_ENABLED_EVENT));
        }
    }

    SE2_Quest_DeactivateAncestors(name);
}

// deactivates all of the ancestors of the given questpoint
void SE2_Quest_DeactivateAncestors(string name) {
    // set up data on the player
    object player = GetFirstPC();
    // get ancestors and deactivate them
    string ancestors = SE2_Quest_GetAllPreceding(name);
    string ancestor;
	
    // for each ancestor
    while (SE2_ArraySize(ancestors) > 0) {
        string ancestor = SE2_GetFirstArrayElement(ancestors);
        ancestors = SE2_RemoveFirstArrayElement(ancestors);
		
		SE2_Quest_setState(player, name, QUEST_POINT_DISABLED);
    }
}

string SE2_Quest_GetAllPreceding(string name) {
    string ancestors = "";
    object player = GetFirstPC();
    string parents = GetLocalString(player, name + QUEST_POINT_PARENTS);

    // for each parent
    while (SE2_ArraySize(parents) > 0) {
        string parent = SE2_GetFirstArrayElement(parents);
        parents = SE2_RemoveFirstArrayElement(parents);
		
        // append the parent as an ancestor
        ancestors = SE2_AppendListElement(ancestors, parent);
		
        // check the parents for ancestors
        ancestors = SE2_AppendListElement(ancestors, SE2_Quest_GetAllPreceding(parent));
    }
    return ancestors;
}

string SE2_Quest_GetAllFollowing(string name) {
    string followers = "";
    object player = GetFirstPC();
    string children = GetLocalString(player, name + QUEST_POINT_CHILDREN);

    // for each child
    while (SE2_ArraySize(children) > 0) {
        string child = SE2_GetFirstArrayElement(children);
        children = SE2_RemoveFirstArrayElement(children);
		
        // append the child as a follower
        followers = SE2_AppendListElement(followers, child);
		
        // add the recursive result
        followers = SE2_AppendListElement(followers, SE2_Quest_GetAllFollowing(child));
    }
    return followers;
}

// sets the questpoint and ancestor nodes to inactive. sets the questpoint to failed. checks if the Quest can now not be succeeded.
void SE2_Quest_FailQuestPoint(string name) {
    object player = GetFirstPC();
	
	SE2_Quest_setState(player, name, QUEST_POINT_FAIL);

    // deactivate all of the ancestors
    SE2_Quest_DeactivateAncestors(name);

    // validate the quest
    //SE2_Quest_ValidateQuest();
}

int SE2_Quest_getState(object oPlayer, string name) {
    return GetLocalInt(oPlayer, name + QUEST_POINT_STATE);
}

void SE2_Quest_setState(object oPlayer, string name, int state) {
    SetLocalInt(oPlayer, name + QUEST_POINT_STATE, state);
}



// registers the given quest in the quest system
//void SE2_Quest_RegisterQuest(string name, string start, string end) {
    // set up data on the player
    //object player = GetFirstPC();
    // set up QUEST_START
    //SetLocalString(player, name + QUEST_START, start);
    // set up QUEST_END
    //SetLocalString(player, name + QUEST_END, end);
    // set up QUEST_ACTIVE default to false
    //SetLocalInt(player, name + QUEST_ACTIVE, 0);
    // set up QUEST_START
    //SetLocalString(player, name + QUEST_CONTAINER, containerQuest);
//}

// activates the quest�s start point
//void SE2_Quest_StartQuest(string name) {
    // set up data on the player
    //object player = GetFirstPC();
    // get the start of the quest
    //string start = getLocalString(player, name + QUEST_START);
    // activate start
    //setLocalInt(player, start + QUEST_POINT_ACTIVE, 1);
//}

//disables all points in the quest
//void SE2_Quest_FailQuest(string name) {
//TODO
//}

// validates the quest system by checking if the quest cannot be succeeded, 
// this should be called after every questpoint failure
//void SE2_Quest_ValidateQuest(string name) {
    // set up data on the player
    //object player = GetFirstPC();
    // get the start of the quest
    //string start = getLocalString(player, name + QUEST_START);
    // check if the questpoint is active
    //int isActive = SE2_Quest_getIsActive(player, start);
    //int succeeded = SE2_Quest_getIsSuceeded(player, start);
    //if (isActive) {
    //
    //} else {
    //  if (succeeded) {
    //      string children = getLocalString(player, start + QUEST_POINT_CHILDREN);
    //      string child;
    //      // for each child
    //      while ((child = SE2_SplitTextByDelimiter(children, SEPARATOR)) != "") {
    //      //TODO FINISH THIS METHOD
    //      }
    //  }
    //}
//}