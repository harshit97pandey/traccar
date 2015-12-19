# [Traccar](https://www.traccar.org)
[![Build Status](https://travis-ci.org/tananaev/traccar.svg?branch=master)](https://travis-ci.org/tananaev/traccar)

## Contacts

Author - Anton Tananaev ([anton.tananaev@gmail.com](mailto:anton.tananaev@gmail.com))

Website - [https://www.traccar.org](https://www.traccar.org)

## Overview

Traccar is open source server for various GPS tracking devices. Project is written in Java and works on most platforms with installed Java Runtime Environment.

## Build

Please read <a href="https://www.traccar.org/build/">build from source documentation</a> on the official website.

## License

    Apache License, Version 2.0

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


--------------------------------


## Geofence ფუნქიონლაის აღწერა


### არე
არე არის რამდენიმე სახის
- პოლიგონი
- წრე
- მარშუტი გარკვეული დიაპაზონით (რადიუსით)

### პერიოდები
დღეები არის სამუშაო და დასვენების დღეებისაგან
დასვენების დღეები შეიძლება იყოს რეგულარული (უიკენდი) ანდ არალრეგულარული (ახალი წელი)
შესაძლებელია ავტომობილი მუშაობდეს შაბათ-კვირას და არ მუშაობდეს ორშაბათს
- ირჩევს დღეებს და საათობრივ პერიოდებს
- ამატებს გამონაკლისეს

### შეზღუდვის ტიპები
- სიჩქარის შეზღუდვა
- მითითებული არის გარეთ არ გასვლა პერიოდში (**მუდმივად**), მაგალითად საღამოს 6-დან დილის 9-მდე ავტომობილი უნდა იყვეს სადგომზე
- მითითებულ არეში გამოცხადება პერიოდში (**ერთხელ მაინც**), მაგალითად დილის 9-დან 10-მდე ავტომობილი უნდა გამოცხადდეს ოფისში
- გაჩერების ხანგრძლივობის კონტროლი

მომხმარებელი ირჩევს
- არეს
- პერიოდს
- შეზღუდვის ტიპს
ინახავს მონაცემთა ბაზაში

სისტემა იღებს ყველა შეზღუდავას და უკეთებს ანალიზს
- მოწყობილობებიდან მოსულ ინფორმაციას (კოორდინატები, სიჩქარე)
- დროის სუალედებით შეზღუდვებს (ხომ არ დაარღვია ავტომობილმა არმოსული კოორდინატით არეში ყოფნის პირობა). **@zgnachvi ეს ნაწილი ტექნოლოგიურად კარგად არის მოსაფიქრებელი** 
