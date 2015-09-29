#include <iostream>
#include <sstream>
#include <iterator>
#include "HEBStory.h"

using namespace std;

/*
 *
 *	This class is used by ScriptEase to implement the Story system with
 *	the Hack-E-Bot robots.
 *
 *	@author ehill
 *
 */

StoryPoint root;
StoryPoint nullPoint;
list<StoryPoint> storyTree;
bool storyInitialized = false;

/**
 * Story Point constructors
 */
StoryPoint::StoryPoint(){

	//Default constructor is for the root and nullpoint only since it is the only StoryPoint
	//we initialize automatically. Has no Parents.
	list<StoryPoint> children;
	this->fanIn = 0;
	this->uniqueName = "Null";
	this->state = DISABLED;
}

StoryPoint::StoryPoint(string uniqueName, int fanIn){

	list<StoryPoint> children, parent;
	this->fanIn = fanIn;
	this->uniqueName = uniqueName;
	this->state = DISABLED;

}

/*
 * Returns the current state of the given StoryPoint.
 */
bool StoryPoint::CheckEnabled(){
	return this->state == ENABLED;
}

bool StoryPoint::CheckSucceeded(){
	return this->state == SUCCEEDED;
}

bool StoryPoint::CheckFailed(){
	return this->state == FAILED;
}

/*
 * Enable the StoryPoint. If the StoryPoint was previously marked as succeeded,
 * we automatically succeed the point.
 */
void StoryPoint::EnableStoryPoint(){

	if(this->state == ENABLED)
		return;

	State previousState = this->state;
	this->state = ENABLED;

	if(previousState == PRESUCCEEDED)
		SucceedStoryPoint(this->uniqueName);
}
/*
 * Succeed the StoryPoint and enable any children that meet their fan in.
 * Sets the state to PRESUCCEEDED if the story point is not yet enabled.
 */
void StoryPoint::SucceedStoryPoint(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);
	StoryPoint *storyPntr = &storyPoint;

	list<StoryPoint>::iterator it, on;

	if(storyPntr->uniqueName != nullPoint.uniqueName){
		//If the State of the StoryPoint is PRE/SUCCEEDED then do nothing
		if(storyPntr->state == PRESUCCEEDED || storyPntr->state == SUCCEEDED) return;

		//If the StoryPoint's state is enabled we need to Succeed it and then all the children
		if(storyPntr->state == ENABLED){
			storyPntr->state = SUCCEEDED;

			//For the children of the StoryPoint
			for(it = storyPntr->children.begin(); it != storyPntr->children.end(); it++){

				if(it->state == ENABLED || it->state == SUCCEEDED) continue;

				//Need to keep track of how many parents have been succeeded. We
				//can't enable a child if not all of it's parent's have been succeeded
				int succeededParents = 0;
				for(on = it->parent.begin(); on != it->parent.end(); on++){
					if(on->CheckSucceeded()) succeededParents++;
				}

				if(succeededParents >= storyPntr->fanIn){
					storyPntr->EnableStoryPoint();
					break;
				}
			}
		} else if (storyPntr->state == DISABLED){
			storyPntr->state = PRESUCCEEDED;
		}
	} else {
		//cout << "Attempted to Succeed nonexistant StoryPoint " << uniqueName << endl;
	}

}

/*
 * Fails the StoryPoint and all of it's children.
 */
void StoryPoint::FailStoryPoint(string uniqueName){
	list<StoryPoint>::iterator it;
	StoryPoint storyPoint = FindStoryPoint(uniqueName);
	StoryPoint *storyPntr = &storyPoint;

	if(storyPntr->uniqueName != nullPoint.uniqueName){
		for(it = storyPntr->children.begin(); it != storyPntr->children.end(); it++){
			it->state = FAILED;
		}
		storyPntr->state = FAILED;
	} else {
		//cout << "Attempted to fail nonexistant StoryPoint " << uniqueName << endl;
	}

}

/*
 * Continue at the specified StoryPoint. We need to disable any descendants it has
 * that might be active, and then enable it.
 */
void StoryPoint::ContinueAtStoryPoint(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);
	StoryPoint *storyPntr = &storyPoint;
	if(storyPntr->uniqueName != nullPoint.uniqueName){
		storyPntr->DisableDescendants();
		storyPntr->EnableStoryPoint();
	}
}


/*
 * Finds the given StoryPoint in the StoryTree.
 */
StoryPoint StoryPoint::FindStoryPoint(string uniqueName){

	//cout << "Inside find story point looking for: " << uniqueName << endl;

	list<StoryPoint>::iterator it;
	for(it = storyTree.begin(); it != storyTree.end(); it++){
		if(it->uniqueName == uniqueName){
			return *it;
		}
		else {
			//cout << "could not find the storypoint in the tree " << uniqueName << endl;
		}
	}

	cout << endl;
	return nullPoint;
}

/*
 * Get the descendants of a StoryPoint object (ie: "this"). Uses a recursive call
 * to iterate through each child, and then all of their children. Different from
 * just a StoryPoint's children.
 *
 * root-->A-->B-->C
 *	\	  \-->D
 *   \
 *    \---->E
 *
 *   EX: Root's Descendants are ABCDE
 *   	 A's descendants are BCD but A's children would be BD
 *   	 E Has none
 */
list<StoryPoint> StoryPoint::GetDescendants(){
	list<StoryPoint> descendants;
	list<StoryPoint> recursiveDesc;
	list<StoryPoint>::iterator it;
	list<StoryPoint>::iterator on;

	for(it = this->children.begin(); it != this->children.end(); it++){
		descendants.push_back(*it);
		recursiveDesc=it->GetDescendants();
		for(on = recursiveDesc.begin(); on != recursiveDesc.end(); on++){
			descendants.push_back(*on);
		}
	}

	return descendants;
}

/*
 * Disable the descendants of a story point. Uses a recursive call to disable all the
 * children's children of the beginning StoryPoint.
 */
void StoryPoint::DisableDescendants(){
	list<StoryPoint>::iterator it;
	for(it = this->children.begin(); it != this->children.end(); it++){
		it->state = DISABLED;
		it->DisableDescendants();
	}
}

/*
 * Get all active Story Points. Not really used anywhere but hey functionality.
 */
list<string> GetAllActive(){
	list<string> active;
	list<StoryPoint>::iterator it;
	list<StoryPoint> descendants = root.GetDescendants();

	for(it = storyTree.begin();it != storyTree.end(); it++){
		if(it->CheckEnabled()){
			active.push_back(it->uniqueName);
		}
	}

}

/*
 * Register the root of the story. Should only be called once since any story should only
 * one root. This is called in the Language Dictionary and is automatically generated.
 * Initializes the storyInitialized variable so we're only initializing the root once
 */
void StoryPoint::RegisterRoot(string uniqueName, int fanIn){
	root.uniqueName = uniqueName;
	root.fanIn = fanIn;
	storyInitialized = true;
	root.EnableStoryPoint();
	storyTree.push_back(root);
}

/*
 * Registers a StoryPoint as a child to the parent. Children can be shared between parents
 * so checks to see if the given Child has already been created. If it hasn't, then creates
 * a new StoryPoint, adds it to the StoryTree and then adds it to the given parent. If it
 * does exist, then just adds it to the given parent.
 */
void StoryPoint::RegisterChild(string parentName, string uniqueName, int fanIn){
	list<StoryPoint>::iterator it;
	StoryPoint parent = FindStoryPoint(parentName);
	StoryPoint *parentPntr = parent;

	if(parent.uniqueName != nullPoint.uniqueName){
		StoryPoint child = FindStoryPoint(uniqueName);
		StoryPoint *childPntr = child;

		if(child.uniqueName == nullPoint.uniqueName){
			StoryPoint newChild = StoryPoint(uniqueName, fanIn);
			storyTree.push_back(newChild);
			parent.AddChild(newChild);
		}

		if(child.uniqueName != nullPoint.uniqueName){
			parent.AddChild(child);
		}
	}
	else {
		cout << "Could not find parent with unique name " << parentName << endl;
	}
}

/*
 * Add a StoryPoint to the list. "This" being references is the parent we are
 * attaching the child to.
 */
void StoryPoint::AddChild(StoryPoint child){
	this->children.push_back(child);
	child.parent.push_back(*this);
}

/*
 * We want to know the details of a story point. Checks parents and children
 */
void StoryPoint::CheckDetails(StoryPoint point){
	list<StoryPoint>::iterator it;

	for(it = point.parent.begin(); it != point.parent.end(); it++){
		cout << point.uniqueName << "'s parent(s): " << it->uniqueName << endl;
	}


	for(it = point.children.begin(); it != point.children.end(); it++){
			cout << point.uniqueName << "'s child(ren): " << it->uniqueName << endl;
	}
}

void StoryPoint::CheckAllDetails(list<StoryPoint> pointlist){
	list<StoryPoint>::iterator it;

	for(it = pointlist.begin(); it != pointlist.end(); it++){
		CheckDetails(*it);
	}

}

list<StoryPoint> StoryPoint::GetParents(string uniqueName){
	StoryPoint point = FindStoryPoint(uniqueName);
	list<StoryPoint> parents;
	list<StoryPoint>::iterator it;

	for(it = point.parent.begin();it != point.parent.end(); it++){
		parents.push_back(*it);
	}
	return parents;
}






