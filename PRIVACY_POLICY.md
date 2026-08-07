# Privacy Policy for Macro Lens

_Last updated: August 6, 2026_

## Overview

Macro Lens is a food tracking app that uses your camera and OpenAI's API to estimate the nutritional content of food you photograph. This policy explains what data the app uses and where it goes.

## Data We Collect

**We do not collect any data.** The developer has no servers, no analytics, and no access to anything you do in the app.

## Data Stored on Your Device

The following is stored locally on your device only:

- **Food log entries** — nutritional estimates and descriptions saved to a local database
- **Your OpenAI API key** — stored in Android's DataStore, accessible only to the app

## Data Sent to Third Parties

When you photograph food, the image is sent to **OpenAI** for analysis. Specifically:

- A JPEG photo of your food is transmitted to OpenAI's API (`api.openai.com`)
- OpenAI returns an estimated nutritional breakdown (calories, protein, carbs, fat)
- Your own OpenAI API key is used for this request — you control the account

OpenAI's handling of this data is governed by their own privacy policy and terms of service. You should review [OpenAI's Privacy Policy](https://openai.com/policies/privacy-policy) for details on how they handle API data.

## Permissions

- **Camera** — used to take photos of food. Photos are not saved to your gallery; they are sent directly to OpenAI and then discarded.
- **Internet** — used solely to communicate with OpenAI's API.

## Data Retention and Deletion

- Your food log is stored locally and can be cleared by uninstalling the app.
- No data is retained by the developer because no data is ever sent to the developer.

## Children's Privacy

This app is not directed at children under 13 and does not knowingly collect information from them.

## Changes to This Policy

If this policy changes, the updated version will be posted in this file with a new date.

## Contact

If you have questions, open an issue at [github.com/kisonecat/macro-lens](https://github.com/kisonecat/macro-lens).
