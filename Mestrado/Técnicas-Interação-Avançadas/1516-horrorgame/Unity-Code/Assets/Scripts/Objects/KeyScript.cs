﻿using UnityEngine;
using System.Collections;

public class KeyScript : MonoBehaviour {

	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
	
		transform.Rotate(Vector3.up, Time.deltaTime*30, Space.World);

	}
}
