import React, { useCallback, useEffect, useState } from "react";
import {
  Button,
  Divider,
  Grid,
  GridItem,
  Modal,
  PageSection,
  Stack,
  StackItem,
  Title,
  Tooltip,
} from "@patternfly/react-core";
import { HelpIcon } from "@patternfly/react-icons";
import { useParams, useHistory } from "react-router-dom";
import { IOutcomeRouteParams } from "../../Audit/types";
import { IOutcome } from "../../Outcome/types";
import { getDecisionFeatureScores, getDecisionOutcome, getDecisionOutcomeDetail } from "../../Shared/api/audit.api";
import OutcomePreview from "../../Outcome/OutcomePreview/OutcomePreview";
import InputDataBrowser from "../../InputData/InputDataBrowser/InputDataBrowser";
import SkeletonInlineStripe from "../../Shared/skeletons/SkeletonInlineStripe";
import SkeletonGrid from "../../Shared/skeletons/SkeletonGrid";
import FeaturesScoreChart from "../FeaturesScoreChart/FeaturesScoreChart";
import { orderBy } from "lodash";
import SkeletonTornadoChart from "../../Shared/skeletons/SkeletonTornadoChart/SkeletonTornadoChart";
import FeaturesScoreTable from "../FeatureScoreTable/FeaturesScoreTable";
import ExplanationSelector from "../ExplanationSelector/ExplanationSelector";
import "./ExplanationView.scss";

export interface IFeatureScores {
  featureName: string;
  featureScore: number;
}

const ExplanationView = () => {
  const { executionId, outcomeId } = useParams<IOutcomeRouteParams>();
  const [outcomeData, setOutcomeData] = useState<IOutcome | null>(null);
  const [outcomesList, setOutcomesList] = useState<IOutcome[] | null>(null);
  const [outcomeDetail, setOutcomeDetail] = useState(null);
  const [featuresScores, setFeaturesScores] = useState<IFeatureScores[] | null>(null);
  const [topFeatures, setTopFeatures] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const handleModalToggle = () => {
    setIsModalOpen(!isModalOpen);
  };
  const history = useHistory();

  const switchExplanation = useCallback(
    (outcomeId: string) => {
      history.push(outcomeId);
    },
    [history]
  );

  useEffect(() => {
    let isMounted = true;
    getDecisionOutcome(executionId)
      .then((response) => {
        if (isMounted) {
          if (response.data && response.data.outcomes) {
            setOutcomesList(response.data.outcomes);
          }
        }
      })
      .catch(() => {});

    getDecisionFeatureScores(executionId)
      .then((response) => {
        if (response.data && response.data.featureImportance) {
          const sortedFeatures = orderBy(response.data.featureImportance, (item) => Math.abs(item.featureScore), "asc");
          if (isMounted) {
            setFeaturesScores(sortedFeatures);
            if (sortedFeatures.length > 10) {
              setTopFeatures(true);
            }
          }
        }
      })
      .catch(() => {});
    return () => {
      isMounted = false;
    };
  }, [executionId, history]);

  useEffect(() => {
    if (outcomesList) {
      let outcome = outcomesList.find((item) => item.outcomeId === outcomeId);
      setOutcomeData(outcome as IOutcome);
    }
  }, [outcomeId, outcomesList]);

  useEffect(() => {
    let isMounted = true;
    getDecisionOutcomeDetail(executionId, outcomeId)
      .then((response) => {
        if (isMounted) {
          if (response && response.data && response.data.outcomeInputs) {
            setOutcomeDetail(response.data.outcomeInputs);
          }
        }
      })
      .catch(() => {});

    return () => {
      isMounted = false;
    };
  }, [executionId, outcomeId]);

  return (
    <section className="explanation-view">
      <PageSection variant="default" className="explanation-view__section">
        <div className="container">
          <Title headingLevel="h2" size="3xl">
            <span>Decision: </span>
            {outcomeData === null ? (
              <SkeletonInlineStripe />
            ) : (
              <span className="explanation-view__title">{outcomeData.outcomeName}</span>
            )}
            {outcomeId !== null && outcomesList !== null && outcomesList.length > 1 && (
              <ExplanationSelector
                outcomesList={outcomesList}
                onDecisionSelection={switchExplanation}
                currentExplanationId={outcomeId}
              />
            )}
          </Title>
        </div>
      </PageSection>
      <section className="container">
        <Divider />
      </section>
      <PageSection variant="default" className="explanation-view__section">
        <div className="container">
          <Stack hasGutter>
            <StackItem>
              <Title headingLevel="h3" size="2xl">
                Decision Outcome
              </Title>
            </StackItem>
            <StackItem>
              {outcomeData === null ? (
                <SkeletonGrid rowsNumber={6} colsNumber={2} />
              ) : (
                <OutcomePreview outcomeData={[outcomeData]} compact={false} />
              )}
            </StackItem>
          </Stack>
        </div>
      </PageSection>
      <PageSection variant="light" className="explanation-view__section">
        <div className="container">
          <Stack hasGutter>
            <StackItem>
              <Title headingLevel="h3" size="2xl">
                Explanation
              </Title>
            </StackItem>
            <StackItem>
              {topFeatures ? (
                <Title headingLevel="h4" size="xl">
                  Top Features Score Chart
                </Title>
              ) : (
                <Title headingLevel="h4" size="xl">
                  Features Score Chart
                </Title>
              )}
              <Grid>
                <GridItem span={9}>
                  <div className="explanation-view__chart">
                    {featuresScores === null ? (
                      <SkeletonTornadoChart valuesCount={10} height={400} />
                    ) : (
                      <FeaturesScoreChart featuresScore={featuresScores} />
                    )}
                    {topFeatures && featuresScores !== null && (
                      <>
                        <Button
                          variant="secondary"
                          type="button"
                          className="all-features-opener"
                          onClick={handleModalToggle}>
                          View Complete Chart
                        </Button>
                        <Modal
                          width={"80%"}
                          title="All Features Score Chart"
                          isOpen={isModalOpen}
                          onClose={handleModalToggle}
                          actions={[
                            <Button key="close" onClick={handleModalToggle}>
                              Close
                            </Button>,
                          ]}>
                          <FeaturesScoreChart featuresScore={featuresScores} large={true} />
                        </Modal>
                      </>
                    )}
                  </div>
                </GridItem>
                <GridItem span={3}>{featuresScores && <FeaturesScoreTable featuresScore={featuresScores} />}</GridItem>
              </Grid>
            </StackItem>
          </Stack>
        </div>
      </PageSection>
      <PageSection className="explanation-view__section">
        <div className="container">
          <Stack hasGutter>
            <StackItem>
              <Title headingLevel="h3" size="2xl">
                <span>Decision Influencing Inputs</span>
                <Tooltip
                  position="auto"
                  content={
                    <div>
                      This section displays all the input that contributed to this specific decision outcome. They can
                      include model inputs (or a subset) or other sub-decisions
                    </div>
                  }>
                  <HelpIcon className="explanation-view__input-help" />
                </Tooltip>
              </Title>
            </StackItem>
            <StackItem>{<InputDataBrowser inputData={outcomeDetail} />}</StackItem>
          </Stack>
        </div>
      </PageSection>
    </section>
  );
};

export default ExplanationView;
