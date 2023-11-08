This is the JSON representation of the latest Jalview release's record on bio.tools

To update:
1. go to https://bio.tools/Jalview
2. log in and scroll down to the 'Update Record' button to open the edit interface.
3. Make any chances to the entry - press Validate to ensure all is good
4. Select the JSON tab and copy paste into

``
cat > utils/biotools/Jalview.json
``

Thanks to Herve Menager for the tutorial on storing bio.tools records with the tool's software repository at [CoFest 2023](https://www.open-bio.org/events/bosc-2023/obf-bosc-collaborationfest-2023)