<div align="center">
    <img src="/img/Logo.png"/>
    <h1>Word Pigeon</h1>
</div>

## Demo 

[Demo Video](https://www.youtube.com/watch?v=rZq6Tsc22yk&t=7s)

## Getting Started
Welcome to Word Pigeon, a hackathon project for Brother Hackathon 2020. Our Android app uses machine learning image classification to make learning language vocabulary more enjoyable. Users are able to capture images of objects and our app will relay the top 3 most likely classifications and their language translations. They can then print the results to the QL-1100 label printer. Our language translations are created using Firebases ML Translation Kit. Original credits for the machine learning code go to Michael Sea with his integration of Tensorflows Inception Model on Android: https://github.com/soum-io/TensorFlowLiteInceptionTutorial

* ***Our app currently only supports translations from English to these languages***
 ***(Based off most learned languages in the United States):***
1. Spanish
2. French
3. German
4. Italian
5. Japanese 

## Problem Space
With many people stuck in there homes they are forced to work, study, and spend most of their time in a small confined space. A problem many students are facing is a lack of motivation and making learning enjoyable at home. 

## Target Audience 
English speaking students of any age looking to improve their foreign language vocabulary. 

## Future Improvements
The app currently is based off of pretrained inception models from TensorFlow and they are not the most accurate. Manually training models and using them instead may improve the accuracy for image classification. Our translations are also handled through firebase ML translation kit. We chose this to gain more experience using Firebase and because it was free compared to the Google Translate API. However Firebase ML translation kit requires the download of 30MB translation models per language so it can quickly slow down the app which is why we limited our app to five popular foreign languages learned in the U.S. Printing is also quite limited and not modular or dynamic. 

## Build/Installation Instructions
* Clone repo locally, then open and build project in Android Studio. 
* Run the app using a USB cable connected to an Android Device 

## Technologies Used

* [Adobe XD](https://www.adobe.com/products/xd.html)
* [Android Studio](https://developer.android.com/studio)
* [Java](https://www.java.com/en/)
* [Firebase](https://firebase.google.com/)

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

* [**James Jhong**](https://github.com/jongjam) 
* [**Brian Jhong**](https://github.com/JhongHawan) 

## MIT License

Copyright 2020 Word Pigeon  

Copyright 2019 Michael Shea

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


