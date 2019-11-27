(function($){
    var _studies = {};
    var _defaultPrefixes = "PREFIX :        <http://www.hdruk.ac.uk/sprint/graph/>\n" +
        "PREFIX test:    <http://www.hdruk.ac.uk/sprint/graph/ontology/test/>\n" +
        "PREFIX onto:    <http://www.hdruk.ac.uk/sprint/graph/ontology/>\n" +
        "PREFIX icd10:   <http://www.hdruk.ac.uk/sprint/graph/ontology/ICD10/>\n" +
        "PREFIX patient: <http://www.hdruk.ac.uk/sprint/graph/patient/>\n" +
        "PREFIX RCTV2:   <http://purl.bioontology.org/ontology/RCTV2/>\n";

    function renderStudies(data){
        swal.close();
        _studies = data;
        console.log(data);
        $('#listStudies')
            .find('option')
            .remove();

        var opt = document.createElement('option');
        opt.value = "-create new study-";
        opt.text =  "-create new study-";
        $('#listStudies').append(opt);

        for(var i=0;i<data.length;i++){
            var opt = document.createElement('option');
            opt.value = data[i].name;
            opt.text =  data[i].name;
            $('#listStudies').append(opt);
            _studies[data[i].name] = data[i];
            $(opt).prop("selected", true);
        }
        resetStudyContent();
        renderRules(_studies[$('#listStudies').val()].rules);
    }

    function resetStudyContent(){
        $('#rulesContainer').html('');
    }

    function renderRules(rules){
        $('#rulesContainer').html('');
        if ($('#listStudies').val() != '-create new study-') {
            var s = '<button id="btnNewRule">+</button>';
            for (var r in rules) {
                s +=
                    '<div class="clsHPOTitle"><input type="text" value="' + r + '"/></div>' +
                    '<textarea>' + rules[r] + '</textarea>';
            }
            $('#rulesContainer').html(s);
            
            $('#btnNewRule').click(function () {
                swal.setDefaults({
                    confirmButtonText: 'Next &rarr;',
                    showCancelButton: true,
                    animation: false,
                    progressSteps: ['1', '2']
                });

                var steps = [
                    {
                        title: 'create rule',
                        text: 'rule name',
                        input: 'text',
                        confirmButtonText: 'next'
                    },
                    {
                        title: 'create rule',
                        text: 'rule definition',
                        input: 'text',
                        confirmButtonText: 'create'
                    }
                ];

                swal.queue(steps).then(function (result) {
                    swal.resetDefaults();
                    swal('create rule...');
                    swal.showLoading();

                    var rules = _studies[$('#listStudies').val()].rules;
                    rules[result[0]] = result[1];

                    qbb.inf.createUpDateStudy($('#listStudies').val(), $.toJSON(rules),function (ret) {
                        console.log(ret);
                        swal({
                            title: "rule created",
                            confirmButtonText: 'ok',
                        });
                    });
                });
            });
        }
    }

    function doQuery(){
        swal('query...');
        var q = _defaultPrefixes + $('#txtQuery').val();
        qbb.inf.doQuery(1, $('#listStudies').val(), q, function (data) {
            swal.close();
            console.log(data);
            renderResult(data);
        });
    }

    function renderResult(data){
        var r = $.parseJSON(data.result);
        var sHead = '';
        for(var i=0;i<r.variables.length;i++){
            sHead += "<td>" + r.variables[i] + "</td>";
        }
        sHead = "<thead>" + sHead + "</thead>";

        var rows = "";
        for(var i=0;i<r.rows.length;i++){
            var row = '';
            for(var j=0;j<r.rows[i].length;j++){
                row += "<td>" +  $("<div>").text(r.rows[i][j]).html(); + "</td>";
            }
            rows += "<tr>" + row + "</tr>";
        }
        $('#divQResult').html("<table>" + sHead + rows + "</table>");
    }

    $(document).ready(function(){

        $('.tabView').click(function(){
            $('.viewContent').hide();
            if ($(this).html() == 'RULES'){
                $('#rulesView').show();
            }
            if ($(this).html() == 'QUERY'){
                $('#queryView').show();
            }
            $('.tabView').removeClass('tabSelected');
            $(this).addClass('tabSelected');
        });

        $('#btnQuery').click(function () {
            doQuery();
        });

        $('#listStudies').on("change", function () {
            resetStudyContent();
            if ($(this).val() == '-create new study-'){
                swal.setDefaults({
                    confirmButtonText: 'Next &rarr;',
                    showCancelButton: true,
                    animation: false,
                    progressSteps: ['1']
                });

                var steps = [
                    {
                        title: 'create study',
                        text: 'study name',
                        input: 'text',
                        confirmButtonText: 'create'
                    }
                ];

                swal.queue(steps).then(function (result) {
                    swal.resetDefaults();
                    swal('create study...');
                    swal.showLoading();

                    qbb.inf.createUpDateStudy(result[0], "",function (ret) {
                        console.log(ret);
                        swal({
                            title: "study created",
                            confirmButtonText: 'ok',
                        });
                    });
                });
            }else{
                renderRules(_studies[$('#listStudies').val()].rules);
            }
        });

        swal("loading studies...");
        qbb.inf.getStudies(renderStudies);
    })

})(this.jQuery)
